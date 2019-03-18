package adt;

import java.util.Collection;
import java.util.HashSet;
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
	//private static final long serialVersionUID = 1L;
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
			data[i] = new Node<adt.Entry<K,V>>(new adt.Entry<K,V>(null));
	
		size = 0;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap(Map<? extends K, ? extends V> copy) {	//FIXME: NoSuchMethodException for V HashMap<init>
		data = new Object[copy.size()];
		
		{
			Node<adt.Entry<K, V>> copyHead = null;
			Node<adt.Entry<K, V>> copyCur = null;
			for(int i = 0; i < copy.size(); i++)
			{
				copyHead = (Node<adt.Entry<K, V>>)((adt.HashMap<K,V>)copy).data[i];
				copyCur = copyHead;
				while(copyCur != null)
				{
					data[i] = new Node<adt.Entry<K, V>>(new adt.Entry<K, V>(copyCur.data.getKey(),copyCur.data.getValue())); 	//Put the data in
					copyCur = copyCur.next;
				}
				
			}
		}
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
		Node<adt.Entry<K,V>> head = (Node<adt.Entry<K,V>>)data[Math.floorMod(HashCode(key),data.length)];
		Node<adt.Entry<K,V>> cur = null;
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
				Node<adt.Entry<K,V>> head = null;
				Node<adt.Entry<K,V>> cur = null;
				for(int i = 0; ( i < data.length ) && ( !found ); i++)	//for all of the node heads....
				{
					head = (Node<adt.Entry<K,V>>) data[i];
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
		Node<adt.Entry<K,V>> head = (HashMap<K, V>.Node<adt.Entry<K,V>>) data[Math.floorMod(HashCode(key),data.length)];
		Node<adt.Entry<K,V>> cur;
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
	@SuppressWarnings("unchecked")
	public V put(K key, V value) {
		Node<adt.Entry<K,V>> head = (HashMap<K, V>.Node<adt.Entry<K,V>>) data[Math.floorMod(HashCode(key),data.length)];
		Node<adt.Entry<K,V>> cur = head;
		
		while(cur.next != null)	//traverse the list until we are adding a node to the cur.next
			cur = cur.next;
		
		cur.next = new Node<adt.Entry<K,V>>(new adt.Entry<K,V>(key,value));
		
		size++; //bump up the size
		
		return value;
	}

	@Override
	@SuppressWarnings("unchecked")
	public V remove(Object key) {
		V value = null;
		if(this.containsKey(key)) //If this contains the key to remove...
		{
			Node<adt.Entry<K,V>> head = (HashMap<K, V>.Node<adt.Entry<K,V>>) data[Math.floorMod(HashCode(key),data.length)];
			Node<adt.Entry<K,V>> cur = head;
			
			while(!((adt.Entry<K, V>)cur.next.data).getKey().equals(key))	//traverse the list until we are just before the one to remove
				cur = cur.next;
			
			value = ((adt.Entry<K, V>)cur.next.data).getValue();
			
			cur.next = cur.next.next;	//set the curent node.next to the one after the removal one
			cur.next.next = null;		//set the removal one.next equal to null to kill all references.
			size--;
		}
		return value;
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
	@SuppressWarnings("unchecked")
	public Set<K> keySet() {
		Set<K> output = new HashSet<K>();
		
		{
			Node<adt.Entry<K,V>> head = null;
			Node<adt.Entry<K,V>> cur = null;
			for(int i = 0; ( i < data.length ); i++)	//for all of the node heads....
			{
				head = (Node<adt.Entry<K,V>>) data[i];
				cur = head.next;
				
				while(cur != null)
				{
					output.add(((adt.Entry<K, V>)cur.data).getKey());
				}
			}
		}
		
		return output;
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
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString()
	{
		String output = "{";
		Node<adt.Entry<K, V>> head = null;
		Node<adt.Entry<K, V>> cur = null;
		
		
		for(int i = 0; i < data.length; i++)
		{
			head = (Node<adt.Entry<K, V>>) data[i];
			cur = head.next;
			
			if(cur != null)
			{
				while(cur != null)
				{
					output += cur.data.getKey() + "=";
					output += cur.data.getValue() + ", ";
					cur = cur.next;
				}
				output = output.substring(0,output.length() - 2) + ", ";
		
			}
		}
		
		output = (output.length() > 2) ? output.substring(0,output.length() - 2) + "}" : "{}";
		
		return output;
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
		@Override
		public String toString()
		{
			return "D: " + data + "REF: " + next;
		}
	}
}