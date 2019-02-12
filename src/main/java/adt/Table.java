package adt;

import adt.HashMap;

/** 
 * This class is a hash map alias providing
 * a Primary Key Value -> Row mapping.
 * 
 * Additional features may be implemented.
 */
public class Table extends HashMap<Object, Row> implements Cloneable{
	private static final long serialVersionUID = 1L;
	
	/** Do not modify. **/
	private Schema schema;
	
	/** Do not modify. **/
	public Schema getSchema() {
		return schema;
	}
	
	/** Do not modify. **/
	public void setSchema(Schema schema) {
		this.schema = schema;
	}
	
	/** Do not modify. **/
	public Table() {
		super();
		setSchema(new Schema());
	}
	
	/** Do not modify. **/
	public Table(Table table) {
		super(table);
		setSchema(new Schema(table.schema));
	}
	
	//Slightly redundant since there is a copy constructor but .clone might be easier to read in the code
	public Table clone()
	{
		return new Table(this);
	}
}