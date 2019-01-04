package driver;

import adt.Response;
import adt.Database;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * Do not remove this driver.
 * 
 * Example:
 * 	ECHO "Hello, world!"
 * 
 * Response:
 * 	success flag
 * 	message "Hello, world!"
 * 	no result table
 */
public class DEcho implements Driver {
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			"ECHO\\s+\"([^\"]*)\"",
			Pattern.CASE_INSENSITIVE
		);
	}

	@Override
	public Response execute(Database db, String query) {
		Matcher matcher = pattern.matcher(query.trim());
		if (!matcher.matches()) return null;

		String text = matcher.group(1);
		
		return new Response(true, text, null);
	}
}
