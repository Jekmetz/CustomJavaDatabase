package driver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Table;

public class DDrop implements Driver {
	//Initialize Variables
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			"DROP\\s+TABLE\\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)",
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
		
		if(!matcher.matches()) 	return null; //If it isn't a full match, return null
		
		String dMessage = (db.remove(matcher.group("tabName")) == null) ? "The table " + matcher.group("tabName") + " did not exist in the database!" : "Table successfully dropped!";
		
		//RETURN VALUES
		table = null;
		message = dMessage;
		
		return new Response(true,message,table);
	}

}
