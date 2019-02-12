package driver;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Row;
import adt.Table;

public class DGetTypes implements Driver {

	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
				//GET\s+TYPES\s(?<tabName>[a-z][a-z0-9_]*)
				"GET\\s+TYPES\\s(?<tabName>[a-z][a-z0-9_]*)",
				Pattern.CASE_INSENSITIVE);
	}

	@Override
	public Response execute(Database db, String query) {
		// Initialize method variable
		Matcher matcher = pattern.matcher(query.trim());

		if (!matcher.matches())
			return null; // If it isn't a full match, return null...
		
		//Init return variables
		boolean success = true;
		String message = null;
		Table table = new Table();
		
		if(db.containsKey(matcher.group("tabName"))) //If the table exists in the database...
		{
			Table dbTable = db.get(matcher.group("tabName"));
			Set<Object> keySet = dbTable.keySet();
			{
				Row dbRow = null;
				Row row = new Row();
				for(Object tabKey : keySet)
				{
					row = new Row();
					dbRow = dbTable.get(tabKey);
					for(Object obj : dbRow)
					{
						row.add(obj.getClass().getSimpleName());
					}
					table.put(tabKey.getClass().getSimpleName(), row);
				}
			}
			table.setSchema(dbTable.getSchema().clone());
			table.getSchema().put("table_name", null);
		}else //If the table does not exist in the database...
		{
			success = false;
			message = "The table \"" + matcher.group("tabName") + "\" does not exist in the database!";
			table = null;
		}
		
		return new Response(success,message,table,(table == null) ? null : table.getSchema());
	}

}
