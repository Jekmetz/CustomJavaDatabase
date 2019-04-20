package adt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** 
 * This class is a hash map alias providing
 * a Primary Key Value -> Row mapping.
 * 
 * Additional features may be implemented.
 */
@XmlRootElement
public class Table extends HashMap<Object, Row> implements Cloneable,Serializable{
	@XmlAttribute(name="version")
	private static final long serialVersionUID = 1L;
	
	/** Do not modify. **/
	private Schema schema;
	
	/** Do not modify. **/
	@XmlElement(name="schema")
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
	
	/*********SERIALIZATION STATION****************/
	
	//@Overide Kinda
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		/* int -> sizeOfRows
		 * Schema -> schema
		 * Row... {table.size} -> data
		 */
		int tableSize = this.size();
		out.writeInt(tableSize);
		out.writeObject(this.getSchema());

		Collection<Row> rows = this.values();
		
		for(Row row : rows)
			out.writeObject(row);
		
	}
	
	//@Override Kinda
	 private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	 {
		 /* int -> sizeOfRows
	      * Schema -> schema
		  * Row... {table.size} -> data
		  */
		
		 int tableSize = in.readInt();
		 Schema schem = (Schema) in.readObject();
		 Integer primIndex = schem.getInteger("primary_index");
		 
		 for(int i = 0; i < tableSize; i++)
		 {
			 Row row = (Row) in.readObject();
			 this.put(row.get(primIndex), row);
		 }
		 
		 this.setSchema(schem);
	 }
	 
	 /*******************/
}