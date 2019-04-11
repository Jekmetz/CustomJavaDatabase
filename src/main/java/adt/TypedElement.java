package adt;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="typedElement")
public class TypedElement {
	private Object data;
	private String type;
	
	public TypedElement() {
		data = null;
		type = null;
	}
	
	public TypedElement(Object data) {
		this.data = data;
		if (data instanceof String)
			this.type = "string";
		else if (data instanceof Integer)
			this.type = "integer";
		else if (data instanceof Boolean)
			this.type = "boolean";
		else 
			this.type = "null";
	}
	
	@XmlAttribute
	public String getType() { return type; }
	
	public void setType(String type) { this.type = type; }
	
	@XmlElement
	public String getData() { return (data != null) ? data.toString() : null; }
	
	public void setData(String data) {
		if (type.equals("string"))
			this.data = data;
		else if (type.equals("integer"))
			this.data = Integer.parseInt(data);
		else if (type.equals("boolean"))
			this.data = Boolean.parseBoolean(data);
		else
			this.data = null;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof TypedElement) {
			TypedElement that = (TypedElement) other;
			return this.type.equals(that.type) && this.data.equals(that.data);
		}
		else return false;
	}
	
	@Override
	public String toString() {
		if (data != null)
			return String.format("Element[%s]", data);
		else
			return "NullElement";
	}
	
	public Object getDataObject() { return data; }
}