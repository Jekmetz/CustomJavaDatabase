package adt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/** 
 * This class is a hash map alias providing
 * a Field Name -> Field Value mapping.
 * 
 * Additional features may be implemented.
 */
@XmlRootElement
public class Schema extends HashMap<String, Object> implements Cloneable,Serializable{
	@XmlAttribute(name="version")
	private static final long serialVersionUID = 1L;

	/** Do not modify. **/
    public Schema() {
    	super();
    }
    
    /** Do not modify. **/
    public Schema(Schema schema) {
    	super(schema);
    }
    
    /** Do not modify. **/
    public String getString(String key) {
    	return (String) get(key);
    }
    
    /** Do not modify. **/
    @SuppressWarnings("unchecked")
	public List<String> getStringList(String key) {
    	return (List<String>) get(key);
    }
    
    /** Do not modify. **/
    public Integer getInteger(String key) {
    	return (Integer) get(key);
    }
    
    @XmlElementWrapper(name="entries")
    @XmlElement(name="entry")
    public Schema getEntries()
    {
    	return this;
    }
    
    //Slightly redundant since there is a copy constructor but .clone might be easier to read in the code
    public Schema clone()
    {
    	return new Schema(this);
    }
    
    /*********SERIALIZATION STATION****************/
	
	//@Overide Kinda
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		/* UTF -> table_name
		 * int -> primary_index
		 * int -> numColNames
		 * UTF...{numColNames} -> colName
		 * UTF...{numColTypes} -> colType
		 */
		int length = this.getStringList("column_names").size();
		List<String> colNames = this.getStringList("column_names");
		List<String> colTypes = this.getStringList("column_types");
		
		out.writeUTF(this.getString("table_name"));
		out.writeInt(this.getInteger("primary_index"));
		out.writeInt(length);
		for(int i = 0; i < length; i++)
			out.writeUTF(colNames.get(i));
		for(int i = 0; i < length; i++)
			out.writeUTF(colTypes.get(i));
	}
	
	//@Override Kinda
	 private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	 {
		 /* UTF -> table_name
		  * int -> primary_index
		  * int -> numColNames
		  * UTF...{numColNames} -> colName
		  * UTF...{numColTypes} -> colType
		  */
		 
		 ArrayList<String> colNames = new ArrayList<String>();
		 ArrayList<String> colTypes = new ArrayList<String>();
		 int length = 0;
		 
		 this.put("table_name", in.readUTF());
		 this.put("primary_index", in.readInt());
		 
		 length = in.readInt();
		 
		 for(int i = 0; i < length; i++)
			 colNames.add(in.readUTF());
		 for(int i = 0; i < length; i++)
			 colTypes.add(in.readUTF());
		 
		 this.put("column_names", colNames);
		 this.put("column_types", colTypes);
		 
	 }
	 
	 /*******************/
}