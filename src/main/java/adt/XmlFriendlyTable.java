package adt;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XmlFriendlyTable {
	//Init vars
	Integer primary_index = 0;
	List<String> column_names = null;
	List<String> column_types = null;
	List<WrappedList> data = null;
	
	public XmlFriendlyTable()
	{
		primary_index = 0;
		column_names = null;
		column_types = null;
		data = null;
	}
	
	public XmlFriendlyTable(Table table)
	{
		this.primary_index = table.getSchema().getInteger("primary_index");
		this.column_names = table.getSchema().getStringList("column_names");
		this.column_types = table.getSchema().getStringList("column_types");
		data = new ArrayList<WrappedList>();
		Set<Object> dataKeySet = table.keySet();
		
		//Build data
		for(Object key : dataKeySet)
		{
			Row row = table.get(key);
			WrappedList wl = new WrappedList();
			wl.list = new ArrayList<String>();

			for(Object obj : row)
			{
				wl.list.add((obj != null) ? obj.toString() : ";:null:;");
			}
			
			data.add(wl);
		}
	}
	
	@XmlElement(name="primary_index")
	public Integer getPrimary_index() { return primary_index; }
	@XmlElementWrapper(name="column_names")
	@XmlElement(name="column_name")
	public List<String> getColumn_names() { return column_names; }
	@XmlElementWrapper(name="column_types")
	@XmlElement(name="column_type")
	public List<String> getColumn_types() { return column_types; }
	@XmlElementWrapper(name="lists")
	@XmlElement(name="data")
	public List<WrappedList> getData() { return data; }
	
	public void setPrimary_index(Integer primary_index) { this.primary_index = primary_index; }
	public void setColumn_names(List<String> column_names) { this.column_names = column_names; }
	public void setColumn_types(List<String> column_types) { this.column_types = column_types; }
	public void setData(List<WrappedList> data) { this.data = data; }
	
	public Table buildTable()
	{
		Table table = new Table();
		Schema schema = new Schema();
		
		schema.put("column_names", column_names);
		schema.put("primary_index", primary_index);
		schema.put("column_types", column_types);

		//Build data
		for(int i = 0; i < data.size(); i++)
		{
			WrappedList wl = data.get(i);
			Row row = new Row();
			Object primObj = convObjType(column_types.get(primary_index),wl.list.get(primary_index));
			
			for(int j = 0; j < wl.list.size(); j++)
				row.add(convObjType(column_types.get(j),wl.list.get(j)));
			
			table.put(primObj, row);
		}
		
		table.setSchema(schema);
		return table;
	}
	
	//Helpers
	private Object convObjType(String objType, String objString)
	{
		Object output = null;
		
		if(objString.equals(";:null:;")) return null;
		
		if (objType.equals("string"))
			output = objString;
		else if (objType.equals("integer"))
			output = Integer.parseInt(objString);
		else if (objType.equals("boolean"))
			output = Boolean.parseBoolean(objString);
		else
			this.data = null;
		return output;
	}
}
