package driver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Row;
import adt.Table;

public class DCreate implements Driver{
	//Initialize Variables
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			"CREATE\\s+TABLE\\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)\\s*\\((?<inside>[a-zA-Z0-9_\\,\\s]+)\\)",
			Pattern.CASE_INSENSITIVE
		);
	}
	
	
	@Override
	public Response execute(Database db, String query) {
		//Initialize Return variables
		Table table = new Table();
		String message = null;
		
		//Init function vars
		Matcher matcher = pattern.matcher(query.trim());
		
		if(!matcher.matches()) 	return null;	//If it isn't a full match, return null
		
		@SuppressWarnings("unused")
		String test = null; //Used to test if groups exist
		Pattern subpattern = Pattern.compile(
			"\\s*(?<primary>PRIMARY(?:\\s+))?(?<type>(?:STRING|BOOLEAN|INTEGER))\\s+(?<name>[a-zA-Z][a-zA-Z0-9_]*)",
			Pattern.CASE_INSENSITIVE		
		);
		Matcher submatcher;
		boolean primaryFound = false;			//primary key found
		String[] args = matcher.group("inside").split(",");		//split by comma
		List<String> names = new ArrayList<String>();
		List<String> types = new ArrayList<String>();
		
		{
			//int i = 0;
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
				
				/*TODO: This structure is wrong, I believe. I think
				 * the rows are not supposed to be named. The rows have one
				 * of each type based off of the column names. I won't put a 
				 * row in the table at all yet. Also, I think that the schema
				 * for primary_name needs to be primary_index and it will be i.
				 * That is just my premonition. Use it as you will, future J.
				 */
				
				//i++;
			}
		}
		
		table.getSchema().put("table_name",matcher.group("tabName"));
		table.getSchema().put("column_names",names);
		table.getSchema().put("column_types",types);
				
		//RETURN VALUES
		//table = null;
		message = "Table successfully created!";
		
		return new Response(true,message,table);
	}

}
