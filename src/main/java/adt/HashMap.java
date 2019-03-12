package adt;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** 
 * This is currently just an alias for a built-in
 * implementation of a hash-based Map and is therefore
 * non-compliant with the Hash modules specification.
 * 
 * You must replace this code with your own hash-based
 * Map implementation when attempting the Hash modules.
 * However, you can continue to use this non-compliant
 * class for all non-Hash modules.
 */
public class HashMap<K,V> extends java.util.HashMap<K,V> implements Map<K,V> {
	private static final long serialVersionUID = 1L;
	
	//Constructors
	public HashMap() {
		super();
	}
	
	public HashMap(Map<? extends K, ? extends V> copy) {
		super(copy);
	}
	
	
	
	//Implemented methods
	private static int HashCode(Object obj)
	{
		if(obj.getClass().getSimpleName().equals("String"))	//If it is a string...
		{
			String str = (String) obj;
			Random random = null;
			double seed = 0;
			
			for(int i = 0; i < str.length(); i++)
				seed += Math.pow(str.charAt(i),str.length() - i);
			
			random = new Random((int)(seed % Integer.MAX_VALUE));
			return (int) random.nextLong();
		} else	//If it is not a string
		{
			return obj.hashCode();
		}
	}
	
	//Node class
	class Node<T> {
		private T data = null;
		private Node<T> next = null;
		
		private Node(T data)
		{
			this.data = data;
			this.next = null;
		}
		
		private Node(T data, Node<T> ref)
		{
			this.data = data;
			this.next = ref;
		}
	}

}