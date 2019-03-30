package driver;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import adt.Database;
import adt.Response;
import adt.Row;
import adt.Table;

public class DExport implements Driver{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			"EXPORT\\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)",
			Pattern.CASE_INSENSITIVE
		);
	}

	/*EXAMPLE:
	 * 	{
	 * 		"schema" : 
	 * 			{
	 * 				"primary_index" : 1,
	 * 				"table_name"    : "tabName",
	 * 				"column_names"	: ["s","pi","b"],
	 * 				"column_types"  : ["string","integer","boolean"]
	 * 			},
	 * 		"data" :
	 * 			{
	 * 				"primary_keys" : [1,2,3,4],
	 * 				"colData"      : [["a",1,true],["b",2,false],["c",3,true],["d",4,false]]
	 * 			}
	 * 	}
	 */
	
	@Override
	public Response execute(Database db, String query) 
	{
		Matcher matcher = pattern.matcher(query.trim());
		if(!matcher.matches()) return null;
		
		if(db.containsKey(matcher.group("tabName")))	//If the table exists in the database
		{
			//Init vars
			Table table = db.get(matcher.group("tabName"));
			String tabName = matcher.group("tabName");
			Integer primaryIndex = table.getSchema().getInteger("primary_index");
			List<String> colNames = table.getSchema().getStringList("column_names");
			List<String> colTypes = table.getSchema().getStringList("column_types");
			Set<Object> primaryKeys = table.keySet();
			
			//Overarching structures
			JsonObjectBuilder jsonObjBuilder = Json.createObjectBuilder();
			JsonObject jsonObj = null;
			
			//Necessary Array Builders
			JsonArrayBuilder colNamesArrayBuilder = Json.createArrayBuilder();
			JsonArrayBuilder colTypesArrayBuilder = Json.createArrayBuilder();
			JsonArrayBuilder primaryKeysArrayBuilder = Json.createArrayBuilder();
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
				/*PRIMARY KEYS ARRAY BUILDER*/
				switch(colTypes.get(primaryIndex))
				{
				case "string":
					primaryKeysArrayBuilder.add(key.toString());
					break;
					
				case "integer":
					primaryKeysArrayBuilder.add(Integer.parseInt(key.toString()));
					break;
					
				case "boolean":
					primaryKeysArrayBuilder.add(Boolean.parseBoolean(key.toString()));
					break;
					
				default:
					primaryKeysArrayBuilder.add("error: key type not string, integer, or boolean");
					break;
				}
				/*****************/

				Row row = table.get(key);
				/*dataArrayBuilder*/
				for(int i = 0; i < colTypes.size(); i++)	//For the length of the coltypes and rows...
				{
					String type = colTypes.get(i);
					
					System.out.println("ROW: " + row + "\nTYPES: " + colTypes + "\ni: " + i + "\n");
					
					JsonArrayBuilder subRow = Json.createArrayBuilder();
					
					switch(type)
					{
					case "string":
						subRow.add(row.get(i).toString());
						
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
					
					dataArrayBuilder.add(subRow);
				}
				/******************/
			}
			
			/*BUILD JSON*/
			jsonObj = jsonObjBuilder
			.add("schema",Json.createObjectBuilder()
					.add("primary_index", primaryIndex)
					.add("table_name", tabName)
					.add("column_names", colNamesArrayBuilder)
					.add("column_types", colTypesArrayBuilder))
			.add("data", Json.createObjectBuilder()
					.add("primary_keys", primaryKeysArrayBuilder)
					.add("data", dataArrayBuilder)
			).build();
			
			System.out.println(jsonObj.toString());
			
		} else //If the table does not exist in the database
		{
			return new Response(false, "The table does not exist in the database!", null);
		}
		
		return new Response(true,"File Exported Successfully",null);
	}
}
