package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import adt.Row;
import adt.Schema;
import adt.Table;
import adt.Utility;
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
		
//		serialize(table.getSchema());
//		
//		Schema newSchem = deserialize();
//		
//		System.out.println("SCHEM: " + table.getSchema() + "\nNEWSCHEM: " + newSchem);
//		
//		
		for(int i = 1; i < 3; i++)
		{
			Row row = new Row();
			row.add(i);
			row.add(Character.toString(64 + i));
			table.put(i, row);
		}
		
//		serialize(table.get(1));
//		
//		Row newRow = deserialize();
//		
//		System.out.println("ROW: " + table.get(1) + "\nNEWROW: " + newRow);
		
		serialize(table);
		
		Table newTab = deserialize();
				
		System.out.println("Table: \n" + Console.formatResponse(table) + "\nnewTab: \n" + Console.formatResponse(newTab));
//		
//		XmlFriendlyTable xmlfo = new XmlFriendlyTable(table);
//		
//		marshall(xmlfo,"testFile");
//		
//		XmlFriendlyTable test = unmarshall("testFile");
//		
//		System.out.println(Console.formatResponse(test.buildTable()));

	}
	
	public static void serialize(Table table)
	{
		FileOutputStream file = null;
		ObjectOutputStream oos = null;
		
		try {
			file = new FileOutputStream(new File(Utility.getRootDirectory("serialize").getAbsolutePath() + "\\testTable.ser"));
			oos = new ObjectOutputStream(file);
			
			oos.writeObject(table);
			
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Table deserialize()
	{
		FileInputStream file = null;
		ObjectInputStream ois = null;
		Table output = null;
		
		try {
			file = new FileInputStream(new File(Utility.getRootDirectory("serialize").getAbsolutePath() + "\\testTable.ser"));
			ois = new ObjectInputStream(file);
			
			output = (Table) ois.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return output;
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
