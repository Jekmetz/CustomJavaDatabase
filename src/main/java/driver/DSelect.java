package driver;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.HashMap;
import adt.Response;
import adt.Table;

public class DSelect implements Driver {
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
				// SELECT\s+(?:\s*(?<colDefs>\*|(?:(?:[A-Za-z][A-Za-z0-9_]*(?:\s+AS\s+[A-Za-z][A-Za-z0-9_]*)?))(?:(?:\s*,\s*)(?:[A-Za-z][A-Za-z0-9_]*(?:\s+AS\s+[A-Za-z][A-Za-z0-9_]*)?))*)\s*)\s+FROM\s+(?<tabName>[A-Za-z][A-Za-z0-9_]*)(?:\s+WHERE\s+(?<lhs>[A-Za-z][A-Za-z0-9_]*)\s+(?<operator>(?:\=|\<\>|\<|\>|\<\=|\>\=))\s+(?<rhs>(?:true|false|null|\"[^\"]*\"|[+-]?(0|[1-9][0-9]*))))?
				"SELECT\\s+(?:\\s*(?<colDefs>\\*|(?:(?:[A-Za-z][A-Za-z0-9_]*(?:\\s+AS\\s+[A-Za-z][A-Za-z0-9_]*)?))(?:(?:\\s*,\\s*)(?:[A-Za-z][A-Za-z0-9_]*(?:\\s+AS\\s+[A-Za-z][A-Za-z0-9_]*)?))*)\\s*)\\s+FROM\\s+(?<tabName>[A-Za-z][A-Za-z0-9_]*)(?:\\s+WHERE\\s+(?<lhs>[A-Za-z][A-Za-z0-9_]*)\\s+(?<operator>(?:\\=|\\<\\>|\\<|\\>|\\<\\=|\\>\\=))\\s+(?<rhs>(?:true|false|null|\\\"[^\\\"]*\\\"|[+-]?(0|[1-9][0-9]*))))?",
				Pattern.CASE_INSENSITIVE);
	}

	@Override
	public Response execute(Database db, String query) {
		// Init function vars
		Matcher matcher = pattern.matcher(query.trim());

		if (!matcher.matches()) return null; // If it isn't a full match, return null
		
		// Initialize Return variables
		Table table = null;
		String message = null;
		boolean success = true;
		
		if(db.containsKey(matcher.group("tabName")))
		{
			//Init table vars
			table = db.get(matcher.group("tabName"));
			List<String> colNames = table.getSchema().getStringList("column_names");
			List<String> colTypes = table.getSchema().getStringList("column_types");
			
			
			//Init constructive vars
			HashMap<String,Integer> aliasMap = new HashMap<String,Integer>();
			
			if(matcher.group("colDefs").contentEquals("*"))
			{
				//for(int )
			}
			
			/* Build AliasSet of all the aliases
			 * 	This will make sure that all of the aliases are unique
			 * Build alias map to map all of the aliases to the values that they exist in the table
			 * See if the left side of the conditional statement is a table name in the table
			 * 	If it is, Do a switch through all of the operators
			 * 
			 */
		} else
		{
			table = null;
			message = "The table name \"" + matcher.group("tabName") + "\" does not exist in the database!";
		}
		
		return new Response(success,message,table,(table == null) ? null : table.getSchema());
	}

}
