package driver;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Row;
import adt.Table;

public class DShow implements Driver{
	//Initialize Variables
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			"SHOW\\s+TABLES",
			Pattern.CASE_INSENSITIVE
		);
	}
	
	@Override
	public Response execute(Database db, String query) {
		//Initialize Return variables
		Table table = new Table();
		//String message = null;
		
		//Init function vars
		Matcher matcher = pattern.matcher(query.trim());
		
		if(!matcher.matches()) 	return null;	//If it isn't a full match, return null
		
		//RETURN VALUES
		table = null;
		Set<String> tabNames = db.keySet();
		
		{
			table = new Table();
			Row row = null;
			
			List<String> names = new ArrayList<String>();
			List<String> types = new ArrayList<String>();
			
			names.add("table_name");
			types.add("string");
			
			names.add("row_count");
			types.add("integer");
			
			table.getSchema().put("column_names", names);
			table.getSchema().put("column_types", types);
			table.getSchema().put("primary_index", 0);
			table.getSchema().put("table_name", null);
			
			for(String tabName : tabNames)
			{
				row = new Row();
				row.add(tabName);
				row.add(db.get(tabName).size());
				table.put(tabName,row);
			}
		}
		//message = (db.toString().equals("{}")) ? "No Tables to Show!" : db.toString();
		
		return new Response(true,table.toString(),null);
	}

}
