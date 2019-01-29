package driver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Table;

public class DInsert implements Driver{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
				//INSERT\s+INTO\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)\s*\(\s*(?<colNames>[a-zA-Z][a-zA-Z0-9_]*(?:(?:\s*,\s*)(?:[a-zA-Z][a-zA-Z0-9_]*))*)\s*\)\s+VALUES\s*\(\s*(?<vals>[a-zA-Z][a-zA-Z0-9_]*(?:(?:\s*,\s*)(?:[a-zA-Z][a-zA-Z0-9_]*))*)\s*\)
				"INSERT\\s+INTO\\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)\\s*\\(\\s*(?<colNames>[a-zA-Z][a-zA-Z0-9_]*(?:(?:\\s*,\\s*)(?:[a-zA-Z][a-zA-Z0-9_]*))*)\\s*\\)\\s+VALUES\\s*\\(\\s*(?<vals>[a-zA-Z][a-zA-Z0-9_]*(?:(?:\\s*,\\s*)(?:[a-zA-Z][a-zA-Z0-9_]*))*)\\s*\\)",
				Pattern.CASE_INSENSITIVE
		);
	}

	@Override
	public Response execute(Database db, String query) {
		//Initialize Return variables
		Table table = null;
		String message = null;
		boolean success = true;
		
		//Init function vars
		Matcher matcher = pattern.matcher(query.trim());
		
		if(!matcher.matches()) 	return null; //If it isn't a full match, return null
		
		return new Response(success,message,table);
	}

}
