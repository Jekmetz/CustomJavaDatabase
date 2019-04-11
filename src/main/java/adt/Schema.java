package adt;

import java.util.List;

import javax.xml.bind.annotation.*;

/** 
 * This class is a hash map alias providing
 * a Field Name -> Field Value mapping.
 * 
 * Additional features may be implemented.
 */
@XmlRootElement
public class Schema extends HashMap<String, Object> implements Cloneable{
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
}