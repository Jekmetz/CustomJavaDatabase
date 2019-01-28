package driver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Table;

public class DCreate implements Driver{
	//Initialize Variables
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//CREATE\s+TABLE\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)\s*\((?<inside>\s*(?:PRIMARY\s+)?(?:(STRING|BOOLEAN|INTEGER))\s+(?:[a-zA-Z][a-zA-Z_0-9]*)((?:\s*\,\s*)(?:PRIMARY\s+)?(?:(STRING|BOOLEAN|INTEGER))\s+(?:[a-zA-Z][a-zA-Z_0-9]*))*)\)
			"CREATE\\s+TABLE\\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)\\s*\\((?<inside>\\s*(?:PRIMARY\\s+)?(?:(STRING|BOOLEAN|INTEGER))\\s+(?:[a-zA-Z][a-zA-Z_0-9]*)((?:\\s*\\,\\s*)(?:PRIMARY\\s+)?(?:(STRING|BOOLEAN|INTEGER))\\s+(?:[a-zA-Z][a-zA-Z_0-9]*))*)\\)",
			Pattern.CASE_INSENSITIVE
		);
	}
	
	
	@Override
	public Response execute(Database db, String query)
       	{
		//Initialize Return variables
		Table table = new Table();
		String message = null;
		boolean success = true;
		
		//Init function vars
		Matcher matcher = pattern.matcher(query.trim());
		
		if(!matcher.matches()) 	return null;	//If it isn't a full match, return null
		
		if(!db.containsKey(matcher.group("tabName")))	//If the table doesn't exist already...
		{
			Set<String> colNames = new HashSet<String>();
			success = false;		//success is false until a primary is found
			boolean dupePrimary = false, dupeColName = false, primaryFound = false;	//various checks
			String[] args = matcher.group("inside").split("\\s*,\\s*");		//split by comma
			List<String> names = new ArrayList<String>();
			List<String> types = new ArrayList<String>();
			
			{
				int i = 0;
				String[] colDefs = null;
				int pFound = 0;
				for(String arg : args)
				{	
					pFound = 0;					//If the primary is found, then the name will be in cD[2] and the type will be cD[1] otherwise... cD[1] and cd[0] respectively
					colDefs = arg.split("\\s+");
									
					if(colDefs.length == 3)		//If it has three elements (ex: primary boolean colName)
					{
						if(!primaryFound)
						{
							pFound = 1;
							primaryFound = true;
							success = true;
							table.getSchema().put("primary_index", i);
						} else
						{
							dupePrimary = true;
							success = false;
						}
					}
					
					if(colNames.add(colDefs[1 + pFound]))	//If there are no dupes...
					{
						names.add(colDefs[1 + pFound]);	//Add the name
						types.add(colDefs[0 + pFound].toLowerCase());	//Add the type
					} else 				//If there are dupes...
					{
						dupeColName = true;
						success = false;
					}
					
					i++;
				}
			}
			
			if(success)	//If there were no bad spots...
			{
				table.getSchema().put("table_name",matcher.group("tabName"));
				table.getSchema().put("column_names",names);
				table.getSchema().put("column_types",types);
						
				db.put(matcher.group("tabName"),table);
				
				//RETURN VALUES
				//table = null;	//table is set to what it needs to be already
				message = "Table Name: " + matcher.group("tabName") + "; Number of Columns: " + names.size();
				//success = true; //success already true if here
				
			} else			//If there was a bad spot
			{
				/*PRIORITY CHAIN
				 * tableExists > dupePrimary > primaryNotFound > dupeColName
				 */
				if(dupePrimary) //If there was more than one primary
				{
					success = false;
					message = "Only one primary column can be specified!";
					table = null;
				} else if (!primaryFound) //If a primary was not found
				{
					success = false;
					message = "Primary column necessary but not included!";
					table = null;
				}else if (dupeColName)	//If there was more than one dupe colName
				{
					success = false;
					message = "No duplicate column names allowed!";
					table = null;
				}
			}
       	} else 			//If the table does exist already
       	{
       		success = false;
       		message = "The table name " + matcher.group("tabName") + " already exists in the database!";
       		table = null;
       	}
		
		return new Response(success,message,table,(table == null) ? null : table.getSchema());
	}

}
