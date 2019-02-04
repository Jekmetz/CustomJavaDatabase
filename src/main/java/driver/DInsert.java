package driver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Row;
import adt.Table;

public class DInsert implements Driver{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
				//INSERT\s+INTO\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)(?:\s*\(\s*(?<colNames>[a-zA-Z][a-zA-Z0-9_]*(?:(?:\s*,\s*)(?:[a-zA-Z][a-zA-Z0-9_]*))*)\s*\))?\s+VALUES\s*\(\s*(?<vals>[a-zA-Z0-9_]+(?:(?:\s*,\s*)(?:[a-zA-Z0-9_]+))*)\s*\)
				"INSERT\\s+INTO\\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)(?:\\s*\\(\\s*(?<colNames>[a-zA-Z][a-zA-Z0-9_]*(?:(?:\\s*,\\s*)(?:[a-zA-Z][a-zA-Z0-9_]*))*)\\s*\\))?\\s+VALUES\\s*\\(\\s*(?<vals>[a-zA-Z0-9_]+(?:(?:\\s*,\\s*)(?:[a-zA-Z0-9_]+))*)\\s*\\)",
				Pattern.CASE_INSENSITIVE
		);
	}

	@Override
	public Response execute(Database db, String query) 
	{
		/*TODO: Fix the column name recognition and
		 * the check for dupe primary in table
		 */
		//Initialize Return variables
		Table table = null;
		String message = null;
		boolean success = true;
		
		//error Flags
		boolean cnDneInTable = false, dupeCn = false, mismatchedValuesColNames = false, dataTypeMismatch = false;
		
		//Init function vars
		Matcher matcher = pattern.matcher(query.trim());
		
		if(!matcher.matches()) 	return null; //If it isn't a full match, return null...
		
		if(db.containsKey(matcher.group("tabName")))	//If the table exists in the database...
		{
			//Init vars for corect database
			table = db.get(matcher.group("tabName"));
			String[] colNames = null, values = null;
			
			if(matcher.group("colNames") != null)	//If the user defined column names...	
			{
				colNames = matcher.group("colNames").split("\\s*,\\s*");
				
				/*VALIDATE COLNAMES*/
				{
					Set<String> colNamesSet = new HashSet<String>();
					for(int i = 0; (i < colNames.length) && !cnDneInTable && !dupeCn; i++)
					{
						if(table.containsKey(colNames[i]))	//Colname is valid
						{
							colNamesSet.add(colNames[i]);
							if(colNamesSet.size() != i+1)	//Not a dupe
							{
								success = false;
								dupeCn = true;
							}
						} else //Colname is not valid
						{
							success = false;
							cnDneInTable = true;
						}
					}
					/***********/
				}
			} else //If the user did not specify column names
			{
				colNames = toStringArray(table.getSchema().getStringList("column_names").toArray());
			}
			
			values = matcher.group("vals").split("\\s*,\\s*");	//Values
			
			if(values.length == colNames.length)	//If the value list has the same number as the columnNames list
			{
				
				Iterator<String> itr = table.getSchema().getStringList("column_names").iterator();
				Row row = new Row();
				
				{
					String colName;
					int index = -1;
					while(itr.hasNext())
					{
						colName = itr.next();
						index = indexOf(colNames,colName);
						if(index != -1)		//If the colName was found in the colNames...
						{
							try
							{
								if(table.getSchema().getStringList("column_types").get(index).equals("integer"))	//If it's an integer...
								{
									row.add(Integer.parseInt(values[index]));
								} else if (table.getSchema().getStringList("column_types").get(index).equals("boolean"))	//If its a Boolean..
								{
									if(values[index].toLowerCase().equals("false"))
									{
										row.add(false);
									} else if (values[index].toLowerCase().equals("true"))
									{
										row.add(true);
									} else
									{
										throw new IllegalArgumentException();
									}
								} else if (table.getSchema().getStringList("column_types").get(index).equals("string"))	//If its a String...
								{
									row.add(values[index]);
								}
							} catch (NumberFormatException e)
							{
								success = false;
								dataTypeMismatch = true;
								message = "Mismatched integer values at index: " + index;
							} catch (IllegalArgumentException e)
							{
								success = false;
								dataTypeMismatch = true;
								message = "Mismatched boolean values at index: " + index;
							}
						}else				//If the colName was not found in the colNames...
						{
							row.add(null);	//add a null to that row
						}
					}
				}
				
				
				if(success) //If nothing went wrong
				{
					table.put(values[table.getSchema().getInteger("primary_index")], row);
					
					success = true;
					message = "Table Name: " + matcher.group("tabName") + "; Number of rows: " + table.size();
				} else		//If something went wrong
				{
					//cnDneInTable = false, dupeCn = false, mismatchedValuesColNames = false, dataTypeMismatch = false
					/*EXCEPTION PRIORITY CHAIN
					 * !existsInTable > cnDneInTable > dupeCn > mismatchedValuesColName > dataTypeMismatch
					 */
					message = null;
					
					if(cnDneInTable)
					{
						message = "Column name specified did not exist in database!";
					}else if(dupeCn)
					{
						message = "Duplicate column name!";
					} else if(mismatchedValuesColNames)
					{
						message = "There is a mismatch of column/value pairs!";
					} else if(dataTypeMismatch)
					{
						//Message already set
					}
					
					success = false;
					table = null;
				}
				
			} else	//If the values length and the colNames length are not equal
			{
				success = false;
				mismatchedValuesColNames = true;
			}
			
		} else	//If the table does not exist in the database
		{
			success = false;
			message = "The table " + matcher.group("tabName") + " does not exist in the database";
			table = null;
		}
		
		return new Response(success,message,table,(table == null) ? null : table.getSchema());
	}
	
	private String[] toStringArray(Object[] arr){
		String[] output = new String[arr.length];
		for(int i = 0; i < arr.length; i++)
			output[i] = (String) arr[i];
		
		return output;
	}
	
	private int indexOf(Object[] arr, Object obj)
	{
		boolean found = false;
		int output = -1;
		for(int i = 0; (i < arr.length) && !found; i++)
		{
			if(arr[i].equals(obj))
			{
				output = i;
				found = true;
			}
		}
		
		return output;
	}

}
