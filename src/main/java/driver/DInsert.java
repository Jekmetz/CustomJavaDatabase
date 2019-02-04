package driver;

import java.util.ArrayList;
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
				//INSERT\s+INTO\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)(?:\s*\(\s*(?<colNames>[a-zA-Z][a-zA-Z0-9_]*(?:(?:\s*,\s*)(?:[a-zA-Z][a-zA-Z0-9_]*))*)\s*\))?\s+VALUES\s*\(\s*(?<vals>[a-zA-Z][a-zA-Z0-9_]*(?:(?:\s*,\s*)(?:[a-zA-Z][a-zA-Z0-9_]*))*)\s*\)
				"INSERT\\s+INTO\\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)(?:\\s*\\(\\s*(?<colNames>[a-zA-Z][a-zA-Z0-9_]*(?:(?:\\s*,\\s*)(?:[a-zA-Z][a-zA-Z0-9_]*))*)\\s*\\))?\\s+VALUES\\s*\\(\\s*(?<vals>[a-zA-Z][a-zA-Z0-9_]*(?:(?:\\s*,\\s*)(?:[a-zA-Z][a-zA-Z0-9_]*))*)\\s*\\)",
				Pattern.CASE_INSENSITIVE
		);
	}

	@Override
	public Response execute(Database db, String query) 
	{
		//Initialize Return variables
		Table table = null;
		String message = null;
		boolean success = true;
		
		//error Flags
		boolean cnDneInTable = false,dupeCn = false;
		
		//Init function vars
		Matcher matcher = pattern.matcher(query.trim());
		
		if(!matcher.matches()) 	return null; //If it isn't a full match, return null...
		
		if(db.containsKey(matcher.group("tabName")))	//If the table exists in the database...
		{
			//Init vars for corect database
			table = db.get(matcher.group("tabName"));
			String[] colNames = null, values = null;
			int[] order = null;
			
			if(matcher.group("colNames") != null)	//If the user defined column names...	
			{
				colNames = matcher.group("colNames").split("\\s*,\\s*");
				order = new int[colNames.length];
				
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
				colNames = (String[]) table.getSchema().getStringList("column_names").toArray();
			}
			
			Iterator<String> itr = table.getSchema().getStringList("column_names").iterator();
			Row row = new Row();
			
			{
				String colName;
				while(itr.hasNext())
				{
					colName = itr.next();
					
				}
			}
			
		} else	//If the table does not exist in the database
		{
			success = false;
			message = "The table " + matcher.group("tabName") + " does not exist in the database";
			table = null;
		}
		
		return new Response(success,message,table);
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
