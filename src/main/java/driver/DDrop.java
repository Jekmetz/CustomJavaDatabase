package driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Table;
import adt.Utility;

public class DDrop implements Driver {
	//Initialize Variables
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			"DROP\\s+TABLE\\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)",
			Pattern.CASE_INSENSITIVE
		);
	}
	
	public boolean isMutation() { return true; }
	
	@Override
	public Response execute(Database db, String query) {
		//Initialize Return variables
		Table table = null;
		String message = null;
		boolean success = true;
		
		//Init function vars
		Matcher matcher = pattern.matcher(query.trim());
		
		if(!matcher.matches()) 	return null; //If it isn't a full match, return null
		
		//##DDropJL001##
		if(db.containsKey(matcher.group("tabName")))
		{
			success = true;
			table = db.get(matcher.group("tabName"));
			//In creating the message, we remove the table
			message = "Table Name: " + matcher.group("tabName") + "; Number of rows: " + db.remove(matcher.group("tabName")).size();
			
			/***********DROP FILE***********/
			try {
				File deleteLog = new File(Utility.getRootDirectory("serialize").getAbsolutePath() + "/droppedTables.txt");
				deleteLog.createNewFile();
				
				FileWriter fw = new FileWriter(deleteLog,true);
				fw.write(table.getSchema().getString("table_name") + "\n");
				fw.close();
			} catch (FileNotFoundException e)
			{
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			/****************/
		} else 
		{
			success = false;
			table = null;
			message = "The table " + matcher.group("tabName") + " did not exist in the database!";
		}

		//RETURN VALUES
		//table = null;		//table already set by this point (label DDropJL001)
		//message = null; 	//message already set by this point (label DDropJL001)
		
		return new Response(success,message,table);
	}

}
