package driver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Row;
import adt.Table;

public class GDSquares implements Driver{
//	private static final Pattern pattern;
//	static {
//		pattern =~ /SQUARES\\s+BELOW\\s+(?<upper>[0-9]+)(?:\\s+AS\\s+(?<baseName>[a-zA-Z][a-zA-Z0-9_]*)(?:\\s*\\,\\s*(?<squareName>[a-zA-Z][a-zA-Z0-9_]*))?)?/
//	}
	
	@Override
	public Response execute(Database db, String query)
    {
		//Initialize response vars
		String message = null;
		def table = new Table();
		
		//Initialize execute vars
		def matcher = query.trim() =~ '(?i)SQUARES\\s+BELOW\\s+(?<upper>[0-9]+)(?:\\s+AS\\s+(?<baseName>[a-zA-Z][a-zA-Z0-9_]*)(?:\\s*\\,\\s*(?<squareName>[a-zA-Z][a-zA-Z0-9_]*))?)?'
		int upper = 0;
		def names = [];
		def types = [];
		def baseName = null, squareName = null;
		def row = null;
		
		if(!matcher.matches()) return null;	//If it didn't match the whole query, return null

		upper = Integer.parseInt(matcher.group("upper"));	
																//Get upper value
		baseName = (matcher.group("baseName") != null) ?: "x"							//set basename (default to "x")
		squareName = (matcher.group("squareName") != null) ?: baseName + "_squared"	//set squarename (default to "x_squared")

		if(baseName.equals(squareName)) return null; 	//don't allow the square and basename to be the same
		
		//Add basename
		names.add(baseName);
		types.add("integer");
		
		//Add squareName
		names.add(squareName);
		types.add("integer");
		
		table.getSchema().put("table_name", null);
		table.getSchema().put("column_names",names);
		table.getSchema().put("column_types", types);
		table.getSchema().put("primary_index", 0);
		
		for(int i = 0; i*i < upper; i++)	//for all the squares under upper...
		{
			row = new Row();				//create a new row,
			row.add(i);						//populate it with the base
			row.add(i*i);					//populate it with the base*base
			table.put(i, row);				//put it in the table with the row name, i
		}
		
		//System.out.println(table.getSchema().toString());
		return new Response(true,message,table,table.getSchema());	//send it back
    }
}
