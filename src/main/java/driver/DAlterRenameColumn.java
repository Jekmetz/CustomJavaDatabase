package driver;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Table;

public class DAlterRenameColumn implements Driver {
	public static final Pattern pattern;
	static {
		pattern = Pattern.compile(
				//ALTER\s+TABLE\s+(?<tabName>[a-z][a-z0-9_]*)\s+RENAME\s+COLUMN\s+(?<oldName>[a-z][a-z0-9_]*)\s+(?<newName>[a-z][a-z0-9_]*)
				"ALTER\\s+TABLE\\s+(?<tabName>[a-z][a-z0-9_]*)\\s+RENAME\\s+COLUMN\\s+(?<oldName>[a-z][a-z0-9_]*)\\s+(?<newName>[a-z][a-z0-9_]*)",
				Pattern.CASE_INSENSITIVE
				);
	}
	
	public boolean isMutation() { return true; }
	
	@Override
	public Response execute(Database db, String query) {
		Matcher matcher = pattern.matcher(query.trim());
		
		if(!matcher.matches()) return null;
		
		//Initialize return vars
		boolean success = true;
		String message = null;
		Table table = null;
		
		//method vars
		boolean dupeColName = false, noOldColName = false;
		
		if(db.containsKey(matcher.group("tabName")))	//If the database contains the table...
		{
			table = db.get(matcher.group("tabName"));
			List<String> colNames = table.getSchema().getStringList("column_names");
			int insertIndex = colNames.indexOf(matcher.group("oldName"));
			
			/*VALIDATE COLNAME and PREDCOLNAME*/
			if( insertIndex == -1 )	//If the old name is not found...
			{
				success = false;
				noOldColName = true;
			}
			if( colNames.indexOf(matcher.group("newName")) != -1 )	//If the new name is found
			{
				success = false;
				dupeColName = true;
			}
			/***************/
			
			if (success)	//If everything is good up to this point...
			{
				colNames.remove(insertIndex); // remove the old colName
				colNames.add(insertIndex, matcher.group("newName"));
				
				//success = true;
				message = "Command completed successfully!";
				//table = table;
			} else //If there was an error...
			{
				if (dupeColName)
				{
					success = false;
					message = "The new column name already exists in the table!";
					table = null;
				} else if (noOldColName)
				{
					success = false;
					message = "The old column name does not exist in the database!";
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
