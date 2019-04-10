package driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import adt.Database;
import adt.Response;
import adt.Row;
import adt.Table;
import adt.XmlFriendlyTable;

public class DExport implements Driver{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//EXPORT\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)\s+(?:TO\s+(?<fileName>[a-zA-Z][a-zA-Z0-9_-]*)\.|AS\s+)(?<fileType>XML|JSON)
			"EXPORT\\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)\\s+(?:TO\\s+(?<fileName>[a-zA-Z][a-zA-Z0-9_-]*)\\.|AS\\s+)(?<fileType>XML|JSON)",
			Pattern.CASE_INSENSITIVE
		);
	}
	
	@Override
	public Response execute(Database db, String query) 
	{
		Matcher matcher = pattern.matcher(query.trim());
		if(!matcher.matches()) return null;
		
		if(db.containsKey(matcher.group("tabName")))	//If the table exists in the database
		{
			//Init vars
			Table table = db.get(matcher.group("tabName"));
			Integer primaryIndex = table.getSchema().getInteger("primary_index");
			List<String> colNames = table.getSchema().getStringList("column_names");
			List<String> colTypes = table.getSchema().getStringList("column_types");
			Set<Object> primaryKeys = table.keySet();
			
			if(matcher.group("fileType").toLowerCase().equals("json"))	//If we are putting this thing into a json file...
			{
				/*EXAMPLE:
				 * 	{
				 * 		"schema" : 
				 * 			{
				 * 				"primary_index" : 1,
				 * 				"column_names"	: ["s","pi","b"],
				 * 				"column_types"  : ["string","integer","boolean"]
				 * 			},
				 * 
				 * 		"colData"      : [["a",1,true],["b",2,false],["c",3,true],["d",4,false]]
				 * 	}
				 */
				
				//Overarching structures
				JsonObjectBuilder jsonObjBuilder = Json.createObjectBuilder();
				JsonObject jsonObj = null;
				
				//Necessary Array Builders
				JsonArrayBuilder colNamesArrayBuilder = Json.createArrayBuilder();
				JsonArrayBuilder colTypesArrayBuilder = Json.createArrayBuilder();
				JsonArrayBuilder dataArrayBuilder = Json.createArrayBuilder();
				
				//colNamesArrayBuilder
				for(String colName : colNames)
					colNamesArrayBuilder.add(colName);
				
				//colTypesArrayBuilder
				for(String colType : colTypes)
					colTypesArrayBuilder.add(colType);
				
				//primaryKeysArrayBuilder & dataArrayBuilder
				for(Object key : primaryKeys)
				{
					Row row = table.get(key);
					JsonArrayBuilder subRow = Json.createArrayBuilder();
					/*dataArrayBuilder*/
					for(int i = 0; i < colTypes.size(); i++)	//For the length of the coltypes and rows...
					{
						String type = colTypes.get(i);
						if(row.get(i) == null)
						{
							subRow.add(":;null;:");
						} else
						{
							switch(type)
							{
							case "string":
								subRow.add(row.get(i).toString());
								break;
								
							case "integer":
								subRow.add(Integer.parseInt(row.get(i).toString()));
								break;
								
							case "boolean":
								subRow.add(Boolean.parseBoolean(row.get(i).toString()));
								break;
								
							default:
								subRow.add("error: addToDataArrayBuilderProblem");
								break;
							}
						}
					}
					dataArrayBuilder.add(subRow);
					/******************/
				}
				
				/*BUILD JSON*/
				jsonObj = jsonObjBuilder
				.add("schema",Json.createObjectBuilder()
						.add("primary_index", primaryIndex)
						.add("column_names", colNamesArrayBuilder)
						.add("column_types", colTypesArrayBuilder))
				.add("data", dataArrayBuilder)
				.build();
				
				try {
					//Make sure that the json directory exists!
					String dir = System.getProperty("user.dir") + "\\json";
					new File(dir).mkdir();
					//Put the file in that directory!
					File file = new File(dir + "\\" + (matcher.group("fileName")!=null ? matcher.group("fileName") : matcher.group("tabName")) + ".json");
					//Write into that file!
					FileWriter fw = new FileWriter(file);
					fw.write(jsonObj.toString());
					//Close that file!
					fw.close();
				}catch(FileNotFoundException e)
				{
					return new Response(false,"Error initializing file creation!",null);
				} catch (IOException e) {
					return new Response(false, "Error creating the file: '" + matcher.group("fileName") + ".json'!",null);
				}
			} else //If we are putting this thing into an xml file...
			{
				marshall(new XmlFriendlyTable(db.get(matcher.group("tabName"))),(matcher.group("fileName") != null) ? matcher.group("fileName") : matcher.group("tabName"));
			}
		} else //If the table does not exist in the database
		{
			return new Response(false, "The table does not exist in the database!", null);
		}
		
		return new Response(true,"File Exported Successfully",null);
	}
	
	public void marshall(XmlFriendlyTable object,String filename) {
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

}
