package driver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;

public class DExport implements Driver{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			"RANGE\\s+([0-9]+)(?:\\s+AS\\s+(\\w+))?",
			Pattern.CASE_INSENSITIVE
		);
	}

	@Override
	public Response execute(Database db, String query) 
	{
		Matcher matcher = pattern.matcher(query.trim());
		if(!matcher.matches()) return null;
		
		return new Response(true,"File Exported Successfully",null);
	}
}
