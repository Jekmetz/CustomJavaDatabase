package driver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.HashMap;
import adt.Response;
import adt.Row;
import adt.Schema;
import adt.Table;

public class DSelect implements Driver {
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
				// SELECT\s+(?:\s*(?<colDefs>\*|(?:(?:[A-Za-z][A-Za-z0-9_]*(?:\s+AS\s+[A-Za-z][A-Za-z0-9_]*)?))(?:(?:\s*,\s*)(?:[A-Za-z][A-Za-z0-9_]*(?:\s+AS\s+[A-Za-z][A-Za-z0-9_]*)?))*)\s*)\s+FROM\s+(?<tabName>[A-Za-z][A-Za-z0-9_]*)(?<where>\s+WHERE\s+(?<lhs>[A-Za-z][A-Za-z0-9_]*)\s*(?<operator>(?:\=|\<\>|\<|\>|\<\=|\>\=))\s*(?<rhs>(?:true|false|null|\"[^\"]*\"|[+-]?(0|[1-9][0-9]*))))?
				"SELECT\\s+(?:\\s*(?<colDefs>\\*|(?:(?:[A-Za-z][A-Za-z0-9_]*(?:\\s+AS\\s+[A-Za-z][A-Za-z0-9_]*)?))(?:(?:\\s*,\\s*)(?:[A-Za-z][A-Za-z0-9_]*(?:\\s+AS\\s+[A-Za-z][A-Za-z0-9_]*)?))*)\\s*)\\s+FROM\\s+(?<tabName>[A-Za-z][A-Za-z0-9_]*)(?<where>\\s+WHERE\\s+(?<lhs>[A-Za-z][A-Za-z0-9_]*)\\s*(?<operator>(?:\\=|\\<\\>|\\<|\\>|\\<\\=|\\>\\=))\\s*(?<rhs>(?:true|false|null|\\\"[^\\\"]*\\\"|[+-]?(0|[1-9][0-9]*))))?",
				Pattern.CASE_INSENSITIVE);
	}

	@Override
	public Response execute(Database db, String query) {
		// Init function vars
		Matcher matcher = pattern.matcher(query.trim());

		if (!matcher.matches()) return null; // If it isn't a full match, return null
		
		//Init computed table vars
		Table computedTable = new Table();
		Schema computedSchema = new Schema();
		ArrayList<String> computedColNames = new ArrayList<String>();
		ArrayList<String> computedColTypes = new ArrayList<String>();
		Integer computedPrimaryIndex = null;
		String computedTableName = null;
		
		if(db.containsKey(matcher.group("tabName")))
		{
			
			//Init table vars
			Table table = db.get(matcher.group("tabName"));
			List<String> colNames = table.getSchema().getStringList("column_names");
			List<String> colTypes = table.getSchema().getStringList("column_types");
			Integer primaryIndex = table.getSchema().getInteger("primary_index");
			
			//Init constructive vars
			HashMap<String,Integer> aliasMap = new HashMap<String,Integer>();
			Set<String> aliasSet = new HashSet<String>();
			
			if(matcher.group("colDefs").contentEquals("*"))	//If they want everything without the aliass...
			{
				for(int i = 0; i < colNames.size(); i++)
				{
					aliasMap.put(colNames.get(i), i);
					
					computedColNames.add(colNames.get(i));
					computedColTypes.add(colTypes.get(i));
				}
				computedPrimaryIndex = primaryIndex;
				
				aliasSet = aliasMap.keySet();
			} else //If they have defined some aliases...
			{
				/*They will be in one of two forms:
				 * 1) <colName>
				 * 2) <colName> AS <alias>
				 * They will be comma separated with optional space
				 */
				String[] aliasDefs = matcher.group("colDefs").split("\\s*,\\s*");
	
				/*CREATE THE <ALIASNAME,TABLEINDEX> MAP AND CHECK FOR DUPEINDEX & FALSECOLNAME & NOPRIMARY AND SET CSCHEMA*/
				{
					String[] aliasDef = null;
					int colIndex = 0;
					int aliasIndex = 0;
					int counter = 0;
	
					for(String aliasString : aliasDefs)
					{
						aliasDef = aliasString.split("\\s+");
						
						aliasIndex = aliasDef.length - 1; //If the length is 1, aliasIndex is 0; if length is 3, alias index is 2
						
						if(!aliasSet.add(aliasDef[aliasIndex]))	//If the aliasDef[0] cannot be added...
							return new Response(false,"The alias \'" + aliasDef[aliasIndex] + "\' was used more than once!",null);	//kill it
						
						colIndex = colNames.indexOf(aliasDef[0]);	//Find colIndex
						
						if(colIndex == -1) 	//If aliasDef[0] does not exist in the columns...
							return new Response(false,"The column name \'" + aliasDef[aliasIndex] + "\' does not exist in the table!",null);
					
						computedColNames.add(aliasDef[aliasIndex]);
						computedColTypes.add(colTypes.get(colIndex));
						aliasMap.put(aliasDef[aliasIndex],colIndex);	//Add it to the map
						
						if(computedPrimaryIndex == null)	//If the computed primary index has not been set yet....	
							if(colIndex == primaryIndex)	//If the column index is the primary index...
								computedPrimaryIndex = counter;
						counter++;
					}
					
					if(computedPrimaryIndex == null) 
					{
						return new Response(false,"The primary column was not specified from the table!",null);
					}
				}
				/************/
						
			}
			computedSchema.put("table_name",computedTableName);
			computedSchema.put("column_names",computedColNames);
			computedSchema.put("column_types",computedColTypes);
			computedSchema.put("primary_index",computedPrimaryIndex);
			computedTable.setSchema(computedSchema);
			/* At this point, the computed table schema is set and there
			 * is a hashmap (aliasMap) that maps the alias name to the 
			 * index in the actual table and these flags have been caught:
			 * noPrimary, dupeAlias, falseColName
			 */
			
			/*SELECT CORRECT ROWS FROM TABLE*/
			
			/*VALIDATE THE LHS AND RHS*/
			boolean where = matcher.group("where") != null;
			if (where)
			{
				Integer compColIndex = colNames.indexOf(matcher.group("lhs"));
				if(compColIndex == -1) //If the lhs is not found...
					return new Response(true,"The left hand side column does not exist in the table!",null);
				
				if(!matcher.group("lhs").equals("null") || !matcher.group("rhs").equals("null"))	//If one of them is not null...
				{
					if(!typeOfString(matcher.group("rhs")).equals(colTypes.get(compColIndex))) //if the type of value does not match the type of column...
						return new Response(false, "The type of value<" + colTypes.get(compColIndex) + "> does not match the value for the column given" + typeOfString(matcher.group("rhs")) + ">!",null);
				
					if(!matcher.group("operator").equals("=") && !matcher.group("operator").equals("<>") && typeOfString(matcher.group("rhs")).equals("boolean"))
						return new Response(false, "Cannot compare booleans with anything other than '=' or '<>'!",null);
				}
			}
			/**********/
			
			/*SET UP ROWS MAYBE WITH OPERATORS (=|<>|<|>|<=|>=)*/
			Set<Object> primVals = table.keySet();
			{
				Row row = null;
				Row valRow = null;
				Integer colIndex = null, lhsIndex = colNames.indexOf(matcher.group("lhs"));
				boolean add = true, string = false, integer = false;
				boolean whereNull = (where && !matcher.group("lhs").equals("null")) && !matcher.group("lhs").equals("null");
				Object lhs = null;
				Object rhs = null;
				
				for(Object primVal : primVals)	//for each of the effective row keys in the table...
				{
					valRow = table.get(primVal); 	// make the val row which is the row we are looking at in the table... 
					/*CHECK TO SEE WHETHER OR NOT TO ADD THE ROW*/
					if(whereNull)	//If the where group exists and one of them is not null
					{
						add = false;	//Assume we are not adding...
						
						lhs = valRow.get(lhsIndex); 				//Set the lhs object
						rhs = convertToType(matcher.group("rhs")); 	//Set the rhs object
						
						if(typeOfString(rhs.toString()).equals("integer")) 
						{
							integer = true;
							string = false;
						} else
						{
							integer = false;
							string = true;
						}
						
						switch(matcher.group("operator"))
						{					
						case "=":
							if(lhs.equals(rhs))
								add = true;
							break;
							
						case "<>":
							if(!lhs.equals(rhs))
								add = true;
							break;
							
						case "<":
							if(string)
							{
								if((lhs.toString()).compareTo(rhs.toString()) < 0)
									add = true;
							} else if (integer)
							{
								if(((Integer)lhs).compareTo((Integer)rhs) < 0)
									add = true;
							}
							break;
							
						case ">":
							if(string)
							{
								System.out.println(((String)lhs).compareTo((String)rhs));
								if(((String)lhs).compareTo((String)rhs) > 0)
									add = true;
							} else if (integer)
							{
								if(((Integer)lhs).compareTo((Integer)rhs) > 0)
									add = true;
							}
							break;
							
						case "<=":
							if(string)
							{
								if((lhs.toString()).compareTo(rhs.toString()) <= 0)
									add = true;
							} else if (integer)
							{
								if(((Integer)lhs).compareTo((Integer)rhs) <= 0)
									add = true;
							}
							break;
							
						case ">=":
							if(string)
							{
								if((lhs.toString()).compareTo(rhs.toString()) <= 0)
									add = true;
							} else if (integer)
							{
								if(((Integer)lhs).compareTo((Integer)rhs) <= 0)
									add = true;
							}
							break;
						}
					}
					/********/
					
					//If there is no where group... then add will always be true...
					
					if(add)	//If we are going to add the row...
					{
						row = new Row(); // make a new row...
						
						for(String colName : computedColNames)	//for each of the columns in the table...
						{
							colIndex = aliasMap.get(colName);
							row.add(valRow.get(colIndex));
						}
						
						computedTable.put(primVal,row);
				
					}
				}
			}
			/************/
			/*******************/
			
		} else
		{
			return new Response(false,"The table name \"" + matcher.group("tabName") + "\" does not exist in the database!",null);
		}

		return new Response(true,"Command completed successfully!",computedTable);
	}
	
	private String typeOfString(String str)
	{
		String output = null;
		
		if((str.charAt(0) == '\"') && (str.charAt(str.length()-1) == '\"'))
			output = "string";
		else if (str.equals("true") || str.equals("false"))
			output = "boolean";
		else if (str.equals("null"))
			output = "null";
		else
			output = "integer";
		
		return output;
	}
	
	private Object convertToType(String str)
	{
		Object output = null;
		
		switch(typeOfString(str))
		{
		case "boolean":
			if(str.equals("true"))
				output = true;
			else
				output = false;
			break;
			
		case "string":
			output = str;
			break;
			
		case "integer":
			output = Integer.parseInt(str);
			break;
			
		case "null":
			output = null;
			break;
		}
		
		return output;
	}

}
