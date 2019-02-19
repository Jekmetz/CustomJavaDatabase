package driver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adt.Database;
import adt.Response;
import adt.Row;
import adt.Table;
import exceptions.NoParensException;
import exceptions.PrimaryNullException;

public class DInsert implements Driver {
	private static final Pattern pattern;
	// [a-zA-Z][a-zA-Z0-9_]*
	private static final String COLNORMAL = "[a-zA-Z][a-zA-Z0-9_]*";
	// (?:true|false|null|\"[^\"]*\"|[+-]?[0-9]+)
	private static final String VALNORMAL = "(?:true|false|null|\\\"[^\\\"]*\\\"|[+-]?(0|[1-9][0-9]*))";
	static {
		pattern = Pattern.compile(
				// INSERT\s+INTO\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)(?:\s*\(\s*(?<colNames>[a-zA-Z][a-zA-Z0-9_]*(?:(?:\s*,\s*)(?:[a-zA-Z][a-zA-Z0-9_]*))*)\s*\))?\s+VALUES\s*\(\s*(?<vals>(?:true|false|null|\"[^\"]+\"|[+-]?[0-9]+)(?:(?:\s*,\s*)(?:(?:true|false|null|\"[^\"]+\"|[+-]?[0-9]+)))*)\s*\)
				"INSERT\\s+INTO\\s+(?<tabName>[a-zA-Z][a-zA-Z0-9_]*)(?:\\s*\\(\\s*(?<colNames>" + COLNORMAL
						+ "(?:(?:\\s*,\\s*)(?:" + COLNORMAL + "))*)\\s*\\))?\\s+VALUES\\s*\\(\\s*(?<vals>" + VALNORMAL
						+ "(?:(?:\\s*,\\s*)(?:" + VALNORMAL + "))*)\\s*\\)",
				Pattern.CASE_INSENSITIVE);
	}

	@Override
	public Response execute(Database db, String query) {
		// Initialize method variables
		HashSet<String> primarySet = null;
		int primaryInt = 0;
		Table computedTable = new Table();
		// Initialize Return variables
		Table table = null;
		String message = null;
		boolean success = true;

		// error Flags
		boolean cnDneInTable = false, dupeCn = false, mismatchedValuesColNames = false, dataTypeMismatch = false,
				pFound = false, dupePrimary = false, primaryNull = false;
		// Init function vars
		Matcher matcher = pattern.matcher(query.trim());

		if (!matcher.matches())
			return null; // If it isn't a full match, return null...

		if (db.containsKey(matcher.group("tabName"))) // If the table exists in the database...
		{
			// Init vars for corect database
			table = db.get(matcher.group("tabName"));
			String[] colNames = null;
			Object[] values = null;
			Object primaryObject = null;

			if (matcher.group("colNames") != null) // If the user defined column names...
			{
				colNames = matcher.group("colNames").split("\\s*,\\s*");
				String primary = table.getSchema().getStringList("column_names").get(table.getSchema().getInteger("primary_index"));
				
				/* VALIDATE COLNAMES */
				{
					Set<String> colNamesSet = new HashSet<String>();
					for (int i = 0; (i < colNames.length) && !cnDneInTable && !dupeCn; i++) 
					{
						if (table.getSchema().getStringList("column_names").contains(colNames[i])) // Colname is valid
						{
							colNamesSet.add(colNames[i]);
							if (colNamesSet.size() != i + 1) // its a dupe
							{
								success = false;
								dupeCn = true;
							}
							if (primary.equals(colNames[i]))	//If we have found our primary...
							{
								pFound = true;
								primaryInt = i;
							}
						} else // Colname is not in table
						{
							success = false;
							cnDneInTable = true;
						}
					}
					
					if (!pFound)	//If we haven't found our primary yet... 
					{
						success = false;
					}
					/***********/
				}
			} else // If the user did not specify column names
			{
				colNames = toStringArray(table.getSchema().getStringList("column_names").toArray());
				primaryInt = indexOf(colNames,table.getSchema().getStringList("column_names").get(table.getSchema().getInteger("primary_index"))); //Get and set the primary index
			}
			if (success) // if everything has succeeded thus far...
			{
				/* INITIALIZE PRIMARYSET */
				primarySet = new HashSet<String>();
				for (Object obj : table.keySet()) 
				{
					primarySet.add(obj.toString());
				}
				/**************/

				values = matcher.group("vals").split("\\s*,\\s*"); // Values

				if (values.length == colNames.length) // If the value list has the same number as the columnNames list
				{

					Iterator<String> itr = table.getSchema().getStringList("column_names").iterator();
					Row row = new Row();
					{
						String colName;
						String value = null;
						int index = -1, counter = 0;
						while (itr.hasNext() && !dataTypeMismatch && !dupePrimary) // For each of the names in
																					// column_names...
						{
							colName = itr.next(); // set the colName to the column we are on...
							index = indexOf(colNames, colName); // Find its index in the array we've specified							
							if (index != -1) // If the colName was found in the colNames...
							{
								value = (String)values[index];
								try {
									
									/* CHECK IF DUPE PRIMARY */
									if (index == primaryInt) // If we are on the primary column
									{
										if (!value.equals("null")) // If they didn't try to add null to the primary val...
										{
											if (!primarySet.add(value.replace("\"",""))) // If there was a problem adding the object to the primary set...
											{
												success = false;
												dupePrimary = true;
											}
										} else // if they tried to add null to the primary value...
										{
											throw new PrimaryNullException();
										}
									}
									/**********/
									
									
									if (table.getSchema().getStringList("column_types").get(counter).equals("integer")) //If its an integer...
									{
										if (!value.equals("null")) // if not null...
										{
											row.add(Integer.parseInt(value));
											if(index == primaryInt) //If it is the primary index...
												primaryObject = Integer.parseInt(value);
										} else // if is null...
										{
											row.add(null);
										}
									} else if (table.getSchema().getStringList("column_types").get(counter).equals("boolean")) // If its a Boolean..
									{
										if (value.toLowerCase().equals("false")) 
										{
											row.add(false);
											if(index == primaryInt) //If it is the primary index...
												primaryObject = false;
										} else if (value.toLowerCase().equals("true"))
										{
											row.add(true);
											if(index == primaryInt) //If it is the primary index...
												primaryObject = true;
										} else if (value.toLowerCase().equals("null"))
										{
											row.add(null);
										} else 
										{
											throw new IllegalArgumentException();
										}
									} else if (table.getSchema().getStringList("column_types").get(counter).equals("string")) // If its a String...
									{
										if (!value.equals("null")) // If not null...
										{
											if ((value.charAt(0) == '\"') && (value.charAt(value.length() - 1) == '\"')) // check if it's surrounded by parenthesis
											{
												values[index] = value.substring(1, value.length() - 1);
												row.add(values[index]);
												if(index == primaryInt) //If it is the primary index...
													primaryObject = values[index];
											} else // if it's not surrounded by parenthesis...
											{
												throw new NoParensException();
											}
										} else // If is null
										{
											row.add(null);
										}
									}

								} catch (NumberFormatException e) 
								{
									success = false;
									dataTypeMismatch = true;
									message = "Mismatched integer values at index: " + index;
								} catch (IllegalArgumentException e) 
								{
									success = false;
									dataTypeMismatch = true;
									message = "Mismatched boolean values at index: " + index;
								} catch (NoParensException e)
								{
									success = false;
									dataTypeMismatch = true;
									message = "Expected parenthesis surrounded string but no parenthesis were found!";
								} catch (PrimaryNullException e) 
								{
									success = false;
									primaryNull = true;
								}
							} else // If the colName was not found in the colNames...
							{
								row.add(null); // add a null to that row
							}

							counter++;
						}
					}
					if (success) // If nothing went wrong
					{
						table.put(primaryObject, row);

						computedTable.put(primaryObject, row);
						computedTable.setSchema(table.getSchema().clone());
						computedTable.getSchema().put("table_name", null);
						success = true;
						message = "Table Name: " + matcher.group("tabName") + "; Number of rows: " + table.size();
					} else // If something went wrong
					{
						// cnDneInTable = false, dupeCn = false, mismatchedValuesColNames = false,
						// dataTypeMismatch = false
						/*
						 * EXCEPTION PRIORITY CHAIN !existsInTable > cnDneInTable > dupeCn > dupePrimary
						 * > mismatchedValuesColName > primaryNull > dataTypeMismatch > !pFound
						 */

						if (cnDneInTable)
						{
							message = "Column name specified did not exist in database!";
						} else if (dupeCn) 
						{
							message = "Duplicate column name!";
						} else if (dupePrimary)
						{
							message = "The primary value must be unique!";
						} else if (mismatchedValuesColNames)
						{
							message = "There is a mismatch of column/value pairs!";
						} else if (primaryNull) 
						{
							message = "The primary value can not be null!";
						} else if (dataTypeMismatch)
						{
							// Message already set
						} else if (!pFound) 
						{
							message = "Primary column necessary but not included!";
						} else // If there is an error we didnt catch...
						{
							message = "Something went wrong and I don't know what! Check for success = false without a flag!";
						}

						success = false;
						table = null;
					}

				} else // If the values length and the colNames length are not equal
				{
					success = false;
					mismatchedValuesColNames = true;
				}

			}
		} else // If the table does not exist in the database
		{
			success = false;
			message = "The table " + matcher.group("tabName") + " does not exist in the database";
			table = null;
		}

		return new Response(success, message, computedTable,
				(computedTable == null) ? null : computedTable.getSchema());
	}

	private String[] toStringArray(Object[] arr) {
		String[] output = new String[arr.length];
		for (int i = 0; i < arr.length; i++)
			output[i] = (String) arr[i];

		return output;
	}

	private int indexOf(Object[] arr, Object... objs) {
		boolean found = false;
		int output = -1;
		for (int i = 0; (i < arr.length) && !found; i++) {
			for (Object obj : objs) {
				if (arr[i].equals(obj)) {
					output = i;
					found = true;
				}
			}
		}

		return output;
	}

}
