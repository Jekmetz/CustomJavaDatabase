package driver;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
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
	public Response execute(Database db, String query) {
		//Initialize Return variables
		Table table = null;
		String message = null;
		boolean success = true;
		
		//Init function vars
		Matcher matcher = pattern.matcher(query.trim());
		
		if(!matcher.matches()) 	return null; //If it isn't a full match, return null...
		
		if(db.containsKey(matcher.group("tabName")))	//If the table exists in the database...
		{
			//Init vars for corect database
			table = db.get(matcher.group("tabName"));
			String[] colNames = null;
			int[] order = null;
			
			if(matcher.group("colNames") != null)	//If the user defined column names...
				colNames = matcher.group("colNames").split("\\s*,\\s*");
			else 		//If the user did not define column names
			{
				colNames = (String[]) table.getSchema().getStringList("column_names").toArray();
				order = new int[colNames.length];
				for(int i = 0; i < colNames.length; i++)
					order[i] = i;		
			}
			
		} else	//If the table does not exist in the database
		{
			success = false;
			message = "The table " + matcher.group("tabName") + " does not exist in the database";
			table = null;
		}
		
		return new Response(success,message,table);
	}

}
