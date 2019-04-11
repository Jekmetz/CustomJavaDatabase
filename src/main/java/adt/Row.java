package adt;

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
public class Row extends ArrayList<Object> {
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
}
