package driver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
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
		String message = null;
		
		//Init function vars
		Matcher matcher = pattern.matcher(query.trim());
		
		if(!matcher.matches()) 	return null;	//If it isn't a full match, return null
		
		//RETURN VALUES
		table = null;
		message = (db.toString().equals("{}")) ? "No Tables to Show!" : db.toString();
		
		return new Response(true,message,table);
	}

}
