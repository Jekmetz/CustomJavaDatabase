package driver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Table;

public class DSelect implements Driver {
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
				// SELECT\s+(?:\(\s*(?<colDefs>\*|(?:(?:[A-Za-z][A-Za-z0-9_]*(?:\s+AS\s+[A-Za-z][A-Za-z0-9_]*)?))(?:(?:\s*,\s*)(?:[A-Za-z][A-Za-z0-9_]*(?:\s+AS\s+[A-Za-z][A-Za-z0-9_]*)?))*)\s*\))\s+FROM\s+(?<tabName>[A-Za-z][A-Za-z0-9_]*)(?:\s+WHERE\s+(?<lhs>[A-Za-z][A-Za-z0-9_]*)\s+(?<operator>(?:\=|\<\>|\<|\>|\<\=|\>\=))\s+(?<rhs>[A-Za-z][A-Za-z0-9_]*))?
				"SELECT\\s+(?:\\(\\s*(?<colDefs>\\*|(?:(?:[A-Za-z][A-Za-z0-9_]*(?:\\s+AS\\s+[A-Za-z][A-Za-z0-9_]*)?))(?:(?:\\s*,\\s*)(?:[A-Za-z][A-Za-z0-9_]*(?:\\s+AS\\s+[A-Za-z][A-Za-z0-9_]*)?))*)\\s*\\))\\s+FROM\\s+(?<tabName>[A-Za-z][A-Za-z0-9_]*)(?:\\s+WHERE\\s+(?<lhs>[A-Za-z][A-Za-z0-9_]*)\\s+(?<operator>(?:\\=|\\<\\>|\\<|\\>|\\<\\=|\\>\\=))\\s+(?<rhs>[A-Za-z][A-Za-z0-9_]*))?",
				Pattern.CASE_INSENSITIVE);
	}

	@Override
	public Response execute(Database db, String query) {
		// Init function vars
		Matcher matcher = pattern.matcher(query.trim());

		if (!matcher.matches()) return null; // If it isn't a full match, return null
		
		// Initialize Return variables
		Table table = new Table();
		String message = null;
		boolean success = true;
		
		if(db.containsKey(matcher.group("tabName")))
		{
			
		} else
		{
			table = null;
			message = "The table name \"" + matcher.group("tabName") + "\" does not exist in the database!";
		}
		
		return new Response(success,message,table,(table == null) ? null : table.getSchema());
	}

}
