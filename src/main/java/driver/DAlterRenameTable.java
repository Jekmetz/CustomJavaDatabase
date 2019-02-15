package driver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Table;

public class DAlterRenameTable implements Driver {
	public static final Pattern pattern;
	static {
		pattern = Pattern.compile(
				//ALTER\s+TABLE\s+(?<tabName>[a-z][a-z0-9_]*)\s+RENAME\s+TO\s+(?<newTabName>[a-z][a-z0-9_]*)
				"ALTER\\s+TABLE\\s+(?<tabName>[a-z][a-z0-9_]*)\\s+RENAME\\s+TO\\s+(?<newTabName>[a-z][a-z0-9_]*)",
				Pattern.CASE_INSENSITIVE
				);
	}
	
	@Override
	public Response execute(Database db, String query) {
		Matcher matcher = pattern.matcher(query.trim());
		
		if(!matcher.matches()) return null;
		
		//Initialize return vars
		boolean success = true;
		String message = null;
		Table table = null;

		if(db.containsKey(matcher.group("tabName")))	//If the database contains the table...
		{
			table = db.remove(matcher.group("tabName"));
			table.getSchema().put("table_name", matcher.group("newTabName"));	//change the schema
			db.put(matcher.group("newTabName"), table);
			
			message = "Command completed Successfully!";
		} else //if the database does not contain the table...
		{
			success = false;
			message = "The table \"" + matcher.group("tabName") + "\" does not exist in the database!";
			table = null;
		}
		
		return new Response(success,message,table,(table == null) ? null : table.getSchema());
	}
}
