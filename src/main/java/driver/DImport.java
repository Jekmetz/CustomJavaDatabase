package driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import adt.Database;
import adt.Response;
import adt.Row;
import adt.Table;
import adt.Utility;
import adt.XmlFriendlyTable;

public class DImport implements Driver{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//IMPORT\s+((?<fileName>[a-zA-Z][a-zA-Z0-9_\-]*)\.(?<fileType>xml|json)\s+TO\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)|(?<options>OPTIONS))
			"IMPORT\\s+((?<fileName>[a-zA-Z][a-zA-Z0-9_\\-]*)\\.(?<fileType>xml|json)\\s+TO\\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)|(?<options>OPTIONS))",
			Pattern.CASE_INSENSITIVE
		);
	}
	
	@Override
	public Response execute(Database db, String query) {
		Matcher matcher = pattern.matcher(query.trim());
		
		if(!matcher.matches()) return null;
		
		Table table = null;
		
		String tabName = validateName(matcher.group("tabName"),db);
		
		if(matcher.group("fileType").toLowerCase().equals("json"))	//If we are reading in json...
		{
			/*EXAMPLE:
			 * 	{
			 * 		"schema" : 
			 * 			{
			 * 				"primary_index" : 1,
			 * 				"column_names"	: ["s","pi","b"],
			 * 				"column_types"  : ["string","integer","boolean"]
			 * 			},
			 * 			"data"      : [["a",1,true],["b",2,false],["c",3,true],["d",4,false]]
			 * 	}
			 */
			
			//init vars for jsonObject
			table = new Table();
			Integer primary_index = null;
			ArrayList<String> colNames = new ArrayList<String>();
			ArrayList<String> colTypes = new ArrayList<String>();
			
			JsonObject jsonObject = null;
			JsonObject schema = null;
			JsonArray data = null;
			
			String dir = System.getProperty("user.dir") + "\\json\\";
			try {
				//Read in the file and make it into a jsonObject
				File file = new File(dir + matcher.group("fileName") + ".json");
				FileReader fr = new FileReader(file);
				JsonReader jsonReader = Json.createReader(fr);
				jsonObject = jsonReader.readObject();
				fr.close();
			} catch (FileNotFoundException e) {
				return new Response(false,"File: '" + matcher.group("fileName") + ".json' not found!",null);
			} catch (IOException e) {
				return new Response(false,"File not closed correctly!",null);
			}
			
			schema = jsonObject.getJsonObject("schema");
			data = jsonObject.getJsonArray("data");
			
			JsonArray colNamesJson = schema.getJsonArray("column_names");
			JsonArray colTypesJson = schema.getJsonArray("column_types");
			
			primary_index = schema.getInt("primary_index");
			
			for(int i = 0; i < colNamesJson.size(); i++)
				colNames.add(colNamesJson.getString(i));
			
			for(int i = 0; i < colTypesJson.size(); i++)
				colTypes.add(colTypesJson.getString(i));
			
			//Set up the schema
			table.getSchema().put("primary_index", primary_index);
			table.getSchema().put("table_name", tabName);
			table.getSchema().put("column_names", colNames);
			table.getSchema().put("column_types", colTypes);
			
			//Get the data in there!
			String primaryType = colTypes.get(primary_index);
			
			for(int i = 0; i < data.size(); i++)
			{
				JsonArray rowJson = data.getJsonArray(i);
				Row row = new Row();
				Object primaryObject = null;
				
				/**Primary Object**/
				switch(primaryType)
				{
				case "string":
					primaryObject = rowJson.get(primary_index);
					break;
					
				case "boolean":
					primaryObject = rowJson.getBoolean(primary_index);
					break;
					
				case "integer":
					primaryObject = rowJson.getInt(primary_index);
					break;
					
				default:
					return new Response(false, "Corrupted data in table: primaryObject!",null);
				}
				/**************/
				
				for(int j = 0; j < rowJson.size(); j++)
				{
					String dataStr = (rowJson.get(j).toString().length() < 3) ? "" : Utility.stripFL(rowJson.get(j).toString());
					if(dataStr.equals(":;null;:"))
						row.add(null);
					else	//If rowJson is supposed to be null
					{
						switch(colTypes.get(j))
						{
						case "string":
							row.add(dataStr);
							break;
							
						case "boolean":
							row.add(rowJson.getBoolean(j));
							break;
							
						case "integer":
							row.add(rowJson.getInt(j));
							break;
							
						default:
							return new Response(false,"Corrupted data in table!",null);
						}
					}
				}
				table.put(primaryObject, row);
			}
			
			db.put(tabName, table);
			
		} else // If we are reading in xml...
		{
			try {
				table = unmarshall(matcher.group("fileName")).buildTable();
			} catch (FileNotFoundException e) {	//IF the file is not found...
				return new Response(false,"File Not Found: '" + matcher.group("fileName") + ".xml'!",null);
			}
			table.getSchema().put("table_name", tabName);
			db.put(tabName, table);
		}
		return new Response(true, "File Imported Successfully!", table);
	}
	
	private String validateName(String tabName, Database db) {
		String output = tabName;
		
		if(db.containsKey(tabName)) output = tabName + "_1";
		
		int i = 1;
		while(db.containsKey(output))
		{
			i++;
			output = tabName.substring(0, tabName.length()-2) + "_" + i;
		}
		
		return output;
	}

	public XmlFriendlyTable unmarshall(String filename) throws FileNotFoundException{
		XmlFriendlyTable result = null;
		try {
			File file = new File(System.getProperty("user.dir") + "\\xml\\" + filename + ".xml");
			if(!file.exists()) throw new FileNotFoundException();
			Unmarshaller unmarshaller = JAXBContext.newInstance(XmlFriendlyTable.class).createUnmarshaller();
			result = (XmlFriendlyTable) unmarshaller.unmarshal(file);
		} 
		catch (JAXBException e) {
			e.printStackTrace();
		}
		return result;
	}
}
