package driver;

import java.util.regex.Pattern;

import adt.Database;
import adt.Response;

public class DInsert implements Driver{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
				//INSERT\s+INTO\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)\s*\(\s*(?<colNames>[a-zA-Z][a-zA-Z0-9_]*((?:\s*,\s*)(?:[a-zA-Z][a-zA-Z0-9_]*))*)\s+\)\s+VALUES\s+
				"",
				Pattern.CASE_INSENSITIVE
		);
	}

	@Override
	public Response execute(Database db, String query) {
		// TODO Auto-generated method stub
		return null;
	}

}
