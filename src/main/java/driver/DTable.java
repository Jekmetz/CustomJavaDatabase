package driver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Row;
import adt.Table;

public class DTable implements Driver {
	//Initialize Variables
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			"^((?<create>CREATE\\s+TABLE\\s+(?<tabNameC>[a-zA-Z][a-zA-Z0-9]*)\\s*\\((?<inside>[a-zA-Z0-9\\,\\s]+)\\)(?:\\s*))|(?<show>SHOW\\s+TABLES(?:\\s*))|(?<drop>DROP\\s+TABLE\\s+(?<tabNameD>[a-zA-Z][a-zA-Z0-9]*)(?:\\s*))){1}$",
			Pattern.CASE_INSENSITIVE
		);
	}

	@Override
	public Response execute(Database db, String query)
       	{
		//response initialization
		Table table = new Table();
		String message = null;
		
		Matcher matcher = pattern.matcher(query.trim());
				
		if(!matcher.matches()) 	return null;

		//Test to see which one worked
		if(matcher.group("create") != null)	//Test Create
		{
			@SuppressWarnings("unused")
			String test = null; //Used to test if groups exist
			Pattern subpattern = Pattern.compile(
				"\\s*(?<primary>PRIMARY(?:\\s+))?(?<type>(?:STRING|BOOLEAN|INTEGER))\\s+(?<name>[a-zA-Z][a-zA-Z0-9]*)",
				Pattern.CASE_INSENSITIVE		
			);
			Matcher submatcher;
			boolean primaryFound = false;			//primary key found
			String[] args = matcher.group("inside").split(",");		//split by comma
			List<String> names = new ArrayList<String>();
			List<String> types = new ArrayList<String>();
			
			{
				int i = 0;
				Row row = null;
				for(String arg : args)
				{	
					submatcher = subpattern.matcher(arg);
					
					if(!submatcher.matches()) return null;

					if(!primaryFound)
					{
						try 
						{
							test = submatcher.group("primary");
							table.getSchema().put("primary_name",submatcher.group("name"));
							table.getSchema().put("primary_type", submatcher.group("type"));
							primaryFound = true;
						} catch (IllegalArgumentException e) { /*Just skip that stuff*/}
					}
					
					types.add(submatcher.group("type"));
					names.add(submatcher.group("name"));
					
					row = new Row();
					table.put(submatcher.group("name"), row);
					
					i++;
				}
			}
			
			table.getSchema().put("table_name",matcher.group("tabNameC"));
			table.getSchema().put("column_names",names);
			table.getSchema().put("column_types",types);
			
			/*
			 * TODO: Make sure to actually put stuff in the table
			 */
			
			//RETURN VALUES
			//table = null;
			message = "Table successfully created!";
		}
		
		if(matcher.group("show") != null)	//TEST SHOW
		{	
			//RETURN VALUES
			table = null;
			message = (db.toString().equals("{}")) ? "No Tables to Show!" : db.toString();
		}
		
		if(matcher.group("drop") != null)
		{
			String dMessage = (db.remove(matcher.group("tabNameD")) == null) ? "The table " + matcher.group("tabNameD") + " did not exist in the database!" : "Table successfully dropped!";
			
			//RETURN VALUES
			table = null;
			message = dMessage;
		}

		return new Response(true,message,table);
	}
}
