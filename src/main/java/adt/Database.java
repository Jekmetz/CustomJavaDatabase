package adt;

import adt.HashMap;

/** 
 * This class is a hash map alias providing
 * a Table Name -> Table Object mapping.
 * 
 * Additional features may be implemented.
 */
public class Database extends HashMap<String, Table> {
	private static final long serialVersionUID = 1L;

	/** Do not modify. **/
	public Database() {
		super();
	}
	
	/** Do not modify. **/
	public Database(Database database) {
		super(database);
	}
}