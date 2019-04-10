package adt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XmlFriendlyTable {
	//Init vars
	String tableName = null;
	Integer primary_index = 0;
	List<String> column_names = null;
	List<String> column_types = null;
	java.util.HashMap<TypedElement,WrappedList> data = null;
	
	public XmlFriendlyTable()
	{
		tableName = null;
		primary_index = 0;
		column_names = null;
		column_types = null;
		data = null;
	}
	
	public XmlFriendlyTable(Table table)
	{
		this.tableName = table.getSchema().getString("table_name");
		this.primary_index = table.getSchema().getInteger("primary_index");
		this.column_names = table.getSchema().getStringList("column_names");
		this.column_types = table.getSchema().getStringList("column_types");
		data = new HashMap<TypedElement,WrappedList>();
		Set<Object> dataKeySet = table.keySet();
		
		//Build data
		for(Object key : dataKeySet)
		{
			ArrayList<TypedElement> row = new ArrayList<TypedElement>();
			WrappedList wl = new WrappedList();
			
			for(Object obj : table.get(key))
				row.add(new TypedElement(obj));
			
			wl.list = row;
			data.put(new TypedElement(key), wl);
		}
	}
	
	@XmlAttribute(name="tableName")
	public String getTableName() { return tableName; }
	@XmlElement(name="primary_index")
	public Integer getPrimary_index() { return primary_index; }
	@XmlElementWrapper(name="column_names")
	@XmlElement(name="column_name")
	public List<String> getColumn_names() { return column_names; }
	@XmlElementWrapper(name="column_types")
	@XmlElement(name="column_type")
	public List<String> getColumn_types() { return column_types; }
	@XmlElement(name="data")
	public java.util.HashMap<TypedElement,WrappedList> getData() { return data; }
	
	public void setTableName(String tableName) { this.tableName = tableName; }
	public void setPrimary_index(Integer primary_index) { this.primary_index = primary_index; }
	public void setColumn_names(List<String> column_names) { this.column_names = column_names; }
	public void setColumn_types(List<String> column_types) { this.column_types = column_types; }
	public void setData(java.util.HashMap<TypedElement,WrappedList> data) { this.data = data; }
	
	public Table buildTable()
	{
		Table table = new Table();
		Schema schema = new Schema();
		
		schema.put("table_name", tableName);
		schema.put("column_names", column_names);
		schema.put("primary_index", primary_index);
		schema.put("column_types", column_types);
		
		Set<TypedElement> dataKeySet = data.keySet();
		
		for(TypedElement te : dataKeySet) 
		{
			WrappedList wl = data.get(te);
			Row row = new Row();
			
			for(TypedElement obj : wl.list)
				row.add(obj.getDataObject());
			
			table.put(te.getDataObject(), row);
		}
		
		table.setSchema(schema);
		return table;
	}
}
