package adt;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="list")
public class WrappedList extends ArrayList<String> {
	
	private static final long serialVersionUID = 1L;
	@XmlElement(name="element")
	public List<TypedElement> list = null;
}
