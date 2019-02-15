package driver;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Table;

public class DAlterDrop implements Driver {
	public static final Pattern pattern;
	static {
		pattern = Pattern.compile(
				//ALTER\s+TABLE\s+(?<tabName>[a-z][a-z0-9_]*)\s+DROP\s+(?<colName>[a-z][a-z0-9_]*)
				"ALTER\\s+TABLE\\s+(?<tabName>[a-z][a-z0-9_]*)\\s+DROP\\s+(?<colName>[a-z][a-z0-9_]*)",
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
		
		//flags
		boolean noColName = false;

		if(db.containsKey(matcher.group("tabName")))	//If the database contains the table...
		{
			table = db.get(matcher.group("tabName"));
			List<String> colNames = table.getSchema().getStringList("column_names");
			int removeIndex = colNames.indexOf(matcher.group("colName"));
			
			if( removeIndex == -1 )	//If the column doesn't exist in the table
			{ 
				success = false;
				noColName = true;
			}
			
			if(success)	//If everything is sanatized and good...
			{
				colNames.remove(removeIndex);
				
				/*REMOVE COLUMN FROM TABLE*/
				Set<Object> keySet = table.keySet();
				for (Object key : keySet)
					table.get(key).remove(removeIndex);	
				/************/
				
				//success = true;
				message = "Command completed successfully!";
				//table = table;
				
			} else	//If something failed...
			{
				if(noColName)
				{
					//success = false;
					message = "The column name did not exist in the table!";
					table = null;
				}
			}
			
		} else //if the database does not contain the table...
		{
			success = false;
			message = "The table \"" + matcher.group("tabName") + "\" does not exist in the database!";
			table = null;
		}
		
		return new Response(success,message,table,(table == null) ? null : table.getSchema());
	}
}
