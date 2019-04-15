package adt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** 
 * This class is a list alias providing
 * a sequence of ordered field values.
 * 
 * Additional features may be implemented.
 */
@XmlRootElement
public class Row extends ArrayList<Object> implements Serializable {
	private static final long serialVersionUID = 1L;

	/** Do not modify. **/
    public Row() {
    	super();
    }
    
    /** Do not modify. **/
    public Row(Row row) {
    	super(row);
    }
    
    @XmlElement(name="e")
    public Row getEntries()
    {
    	return this;
    }
    
    /*********SERIALIZATION STATION****************/
	
	//@Overide Kinda
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		/* int -> length //This is for readObject
		 * Char('i','s','b','n')...{Schema.colNamesLength} alt UTF...{Schema.colNamesLength}  -> Object
		 */
		int length = this.size();
		out.writeInt(length);
		
		for(int i = 0; i < length; i++)
		{
			Object obj = this.get(i);
			
			if(obj == null)
				out.writeChar('n');
			else if (obj instanceof String)
			{
				out.writeChar('s');
				out.writeUTF(obj.toString());
			}else if (obj instanceof Integer)
			{
				out.writeChar('i');
				out.writeInt((Integer) obj);
			}else if (obj instanceof Boolean)
			{
				out.writeChar('b');
				out.writeBoolean((Boolean) obj);
			}else
				throw new IOException("Illegal Data!");
		}
	}
	
	//@Override Kinda
	 private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	 {
		 /* int -> length //This is for readObject
		  * Char('i','s','b','n')...{Schema.colNamesLength} alt UTF...{Schema.colNamesLength}  -> Object
		  */
		 this.clear();
		 int length = in.readInt();
		 
		 for(int i = 0; i < length; i++)
		 {
			 char type = in.readChar();
			 switch(type)
			 {
			 case 'n':
				 this.add(null);
				 break;
				 
			 case 's':
				 this.add(in.readUTF());
				 break;
				 
			 case 'i':
				 this.add(in.readInt());
				 break;
				 
			 case 'b':
				 this.add(in.readBoolean());
				 break;
			 }
		 }
	 }
	 
	 /*******************/
}
