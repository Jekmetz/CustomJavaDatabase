package driver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Table;

public class DDump implements Driver {
	//Initialize vars
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
				//DUMP\s+TABLE\s(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)
				"DUMP\\s+TABLE\\s(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)",
				Pattern.CASE_INSENSITIVE
		);
	}
	
	public boolean isMutation() { return true; }

	@Override public Response execute(Database db, String query)
	{
		//Initialize Return variables
		Table table = null;
		String message = null;
		Boolean success = false;

		//Init function vars
		Matcher matcher = pattern.matcher(query.trim());

		if(!matcher.matches()) return null;
		
		if(db.containsKey(matcher.group("tabName")))	//If the table exists
		{
			table = db.get(matcher.group("tabName"));
			message = null;
			success = true;
		} else
		{
			table = null;
			message = "The table \"" + matcher.group("tabName") + "\" does not exist in the database";
			success = false;
		}
				
		return new Response(success,message,table,(table == null) ? null : table.getSchema());
	}
}
