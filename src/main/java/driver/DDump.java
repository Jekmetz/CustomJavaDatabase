package driver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Row;
import adt.Table;

public class DDump implements Driver {
	//Initialize vars
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
		"",
		Pattern.CASE_INSENSITIVE
		);
	}

	@Override public Response execute(Database db, String query)
	{
		//Initialize Return variables
		Table table = new Table();
		String message = null;

		//Init function vars
		Matcher matcher = pattern.matcher(query.trim());

		if(!matcher.matches()) return null;
	}
}
