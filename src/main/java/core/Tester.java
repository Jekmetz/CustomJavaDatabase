package core;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import adt.Row;
import adt.Table;
import adt.XmlFriendlyTable;

public class Tester {
	
	public static void main(String[] args)
	{
		Table table = new Table();
		
		table.getSchema().put("table_name", "tabName");
		table.getSchema().put("primary_index", 0);
		ArrayList<String> names = new ArrayList<String>();
		names.add("pi");
		names.add("s");
		table.getSchema().put("column_names", names);
		ArrayList<String> types = new ArrayList<String>();
		types.add("integer");
		types.add("string");
		table.getSchema().put("column_types", types);
		
		
		for(int i = 1; i < 3; i++)
		{
			Row row = new Row();
			row.add(i);
			row.add(Character.toString(64 + i));
			table.put(i, row);
		}
		
		XmlFriendlyTable xmlfo = new XmlFriendlyTable(table);
		
		marshall(xmlfo,"testFile");
		
		XmlFriendlyTable test = unmarshall("testFile");
		
		System.out.println(Console.formatResponse(test.buildTable()));

	}

	public static void marshall(XmlFriendlyTable object,String filename) {
		try {
			Marshaller marshaller = JAXBContext.newInstance(XmlFriendlyTable.class).createMarshaller();
		    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		    
		    new File(System.getProperty("user.dir") + "\\xml").mkdir();
		    
		    marshaller.marshal(object, new File(System.getProperty("user.dir") + "\\xml\\" + filename + ".xml"));
		} 
		catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	public static XmlFriendlyTable unmarshall(String filename) {
		XmlFriendlyTable result = null;
		try {
			Unmarshaller unmarshaller = JAXBContext.newInstance(XmlFriendlyTable.class).createUnmarshaller();
			result = (XmlFriendlyTable) unmarshaller.unmarshal(new File(System.getProperty("user.dir") + "\\xml\\" + filename + ".xml"));
		} 
		catch (JAXBException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static ArrayList<Object> concat(ArrayList<Object> temp1, ArrayList<Object> temp2)
	{
		ArrayList<Object> output = new ArrayList<Object>();
		
		for(int i = 0; i < temp1.size(); i++) {
			output.add(temp1.get(i));
		}
		for(int i = 0; i < temp2.size(); i++) {
			output.add(temp2.get(i));
		}
		
		return output;
	}
}
