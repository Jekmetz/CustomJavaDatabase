package driver;

import adt.Response;
import adt.Database;
import adt.Table;
import adt.Row;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * This example driver can be removed.
 * 
 * Examples:
 * 	RANGE 5
 * 	RANGE 3 AS x
 * 
 * Response 1:
 * 	success flag
 * 	no message
 * 	result table
 * 		primary integer column "number"
 *		rows [0]; [1]; [2]; [3]; [4]
 * 
 * Response 2:
 * 	success flag
 * 	no message
 * 	result table
 * 		primary integer column "x"
 *		rows [0]; [1]; [2]
 */
@Deprecated
public class DRange implements Driver {
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			"RANGE\\s+([0-9]+)(?:\\s+AS\\s+(\\w+))?",
			Pattern.CASE_INSENSITIVE
		);
	}

	@Override
	public Response execute(Database db, String query) {
		Matcher matcher = pattern.matcher(query.trim());
		if (!matcher.matches()) return null;

		int upper = Integer.parseInt(matcher.group(1));
		String name = matcher.group(2) != null ? matcher.group(2) : "number";
		
		Table table = new Table();
		
		table.getSchema().put("table_name", null);
		
		List<String> names = new ArrayList<>();
		names.add(name);
		table.getSchema().put("column_names", names);
		
		List<String> types = new ArrayList<>();
		types.add("integer");
		table.getSchema().put("column_types", types);
		
		table.getSchema().put("primary_index", 0);
		
		for (int i = 0; i < upper; i++) {
			Row row = new Row();
			row.add(i);
			table.put(i, row);
		}
		
		return new Response(true, null, table);
	}
}
