package driver;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Table;

public class DAlterInsert implements Driver {
	public static final Pattern pattern;
	static {
		pattern = Pattern.compile(
				//ALTER\s+TABLE\s+(?<tabName>[a-z][a-z0-9_]*)\s+INSERT\s+COLUMN\s+(?<colType>STRING|BOOLEAN|INTEGER)\s+(?<colName>[a-z][a-z0-9_]*)\s+(?<colWhere>(?:FIRST|AFTER\s+(?<predColName>[a-z][a-z0-9_]*)|LAST))?
				"ALTER\\s+TABLE\\s+(?<tabName>[a-z][a-z0-9_]*)\\s+INSERT\\s+COLUMN\\s+(?<colType>STRING|BOOLEAN|INTEGER)\\s+(?<colName>[a-z][a-z0-9_]*)\\s+(?<colWhere>(?:FIRST|AFTER\\s+(?<predColName>[a-z][a-z0-9_]*)|LAST))?",
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
		boolean dupeColName = false, noPredColName = false;
		int insertIndex = -2;	//Initialize it as something recognizable
		
		if(db.containsKey(matcher.group("tabName")))	//If the database contains the table...
		{
			table = db.get(matcher.group("tabName"));									//Initialize table to the literal table in the database
			List<String> colNames = table.getSchema().getStringList("column_names");	//Initialize colNames to a reference from the table
			Integer primaryIndex = table.getSchema().getInteger("primary_index");		//Initialize primaryIndex to a reference from the table
			
			/*VALIDATE COLNAME and PREDCOLNAME*/
			if(colNames.indexOf(matcher.group("colName")) != -1)	//If the new colname already exists in the table...
			{
				success = false;		//Nope!
				dupeColName = true;		//Set the flag
			}
			if( (matcher.group("predColName") != null) && (colNames.indexOf(matcher.group("predColName")) == -1) )	//If the predColName does not exist in the database...
			{
				success = false;		//Nope!
				noPredColName = true;	//Set the flag
			}
			/***************/
			
			if (success)	//If everything is good up to this point...
			{
				String colWhere = matcher.group("colWhere");	
				if(matcher.group("colWhere") == null)			//If it doesn't exist, Treat it as if it were last
					colWhere = "last";

				switch(colWhere.toLowerCase())		//Switching through possible values of colWhere...
				{
				case "first":													//If first...
					insertIndex = 0;											//set insertIndex to 0
					table.getSchema().put("primary_index", primaryIndex+1);		//the primary index has moved up one
					colNames.add(insertIndex, matcher.group("colName"));		//Add the name to the first position in the column_names schema
					break;
					
				case "last":								//If last...
					insertIndex = -1;						//insertIndex = -1 signals that
					colNames.add(matcher.group("colName"));	//Add it to the end
					break;
					
				default:																//If is after <predColName>
					insertIndex = colNames.indexOf(matcher.group("predColName")) + 1;	//insertIndex = find the index of the predCol + 1
					if(insertIndex <= primaryIndex)										//if we are inserting before or at the primary index...
						table.getSchema().put("primary_index", primaryIndex + 1);		//the primary index has moved up one
					colNames.add(insertIndex,matcher.group("colName"));					//add the colName where it is supposed to go
					break;
				}
				
				if(insertIndex == -1)	//If inserting at the end...
					table.getSchema().getStringList("column_types").add(matcher.group("colType").toLowerCase());
				else	//If inserting at insertIndex...
					table.getSchema().getStringList("column_types").add(insertIndex,matcher.group("colType").toLowerCase());
				
				/*INSERT ALL THE NULLS*/
				if ( table.size() != 0 )	//If the list is not empty
				{
					Set<Object> keySet = table.keySet();
					if ( insertIndex == -1 )	//If inserting at the end
					{
						for (Object key : keySet)
							table.get(key).add(null);
					} else			//If inserting in the beginning or the middle
					{
						for (Object key : keySet)
							table.get(key).add(insertIndex,null);	
					}
				}
				/**********/
				
			}
			
			if (success) //If everything finished well enough...
			{
				//success = true;
				message = "Command completed successfully!";
				//table = table;
			} else //If there was an error...
			{
				if (dupeColName)
				{
					success = false;
					message = "That column name already exists in the table!";
					table = null;
				} else if (noPredColName)
				{
					success = false;
					message = "The preceding column name does not exist in the database!";
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
