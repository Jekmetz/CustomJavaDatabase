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
public class HashMap<K,V> implements Map<K,V> {
	//Initialize Vars
	private static final long serialVersionUID = 1L;
	private final int INITIAL_SIZE = 11;
	private int size = 0;
	private Object[] data = null;
	//Constructors
	/**
	 * Constructor that creates new Node array with size INITIAL_SIZE
	 */
	public HashMap() {
		data = new Object[INITIAL_SIZE];
		for(int i = 0; i < INITIAL_SIZE; i++)
			data[i] = new Node<Object>(new adt.Entry<K,V>(null));
	
		size = 0;
	}
	
	public HashMap(Map<? extends K, ? extends V> copy) {
		//super(copy);
	}
	
	
	//Implemented methods
	@Override
	public int size() { return size; }

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean containsKey(Object key) {
		Node<Object> head = (Node<Object>)data[HashCode(key) % data.length];
		Node<Object> cur = null;
		boolean found = false;
		
		cur = head.next;
		while(( cur != null ) && ( !found ))	//while we are not at the end of the list and we have not found it...
		{
			if(((adt.Entry<K,V>)cur.data).getKey().equals(key))	//If the key is in that position...
				found = true;
			
			cur = cur.next;
		}
		
		return found;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean containsValue(Object value) {
		boolean found = false;
		
		if(this.size() != 0)
		{
			{
				Node<Object> head = null;
				Node<Object> cur = null;
				for(int i = 0; ( i < data.length ) && ( !found ); i++)	//for all of the node heads....
				{
					head = (Node<Object>) data[i];
					cur = head.next;
					
					while(( cur != null ) && ( !found ))
					{
						if(((adt.Entry<K,V>)cur.data).getValue().equals(value))
							found = true;
						cur = cur.next;
					}
				}
			}
		}
		
		return found;
	}

	@Override
	@SuppressWarnings("unchecked")
	public V get(Object key) {
		Node<Object> head = (HashMap<K, V>.Node<Object>) data[HashCode(key) % data.length];
		Node<Object> cur;
		boolean found = false;
		V output = null;
		
		if(this.containsKey(key))	//If the key exists in the structure...
		{
			cur = head.next;
			while(( cur != null ) && ( !found ))	//for all the items in the list and while it's not found...
			{
				if(((adt.Entry<K, V>)cur.data).getKey().equals(key))
				{
					output = ((adt.Entry<K, V>)cur.data).getValue();
					found = true;
				}
				
				cur = cur.next;
			}
		}
		
		return output;
	}

	@Override
	public V put(K key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<K> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<V> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	
	//Extra methods
	private int HashCode(Object obj)
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