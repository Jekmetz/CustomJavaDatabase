package adt;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
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
			data[i] = new Node<MapEntry<K,V>>(new MapEntry<K,V>(null));
	
		size = 0;
	}
	
	public HashMap(Map<? extends K,? extends V> map) {
		this();
		this.putAll(map);
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
		Node<MapEntry<K,V>> head = (Node<MapEntry<K,V>>)data[Math.floorMod(HashCode(key),data.length)];
		Node<MapEntry<K,V>> cur = null;
		boolean found = false;
		
		cur = head.next;
		while(( cur != null ) && ( !found ))	//while we are not at the end of the list and we have not found it...
		{
			if(((MapEntry<K,V>)cur.data).getKey().equals(key))	//If the key is in that position...
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
				Node<MapEntry<K,V>> head = null;
				Node<MapEntry<K,V>> cur = null;
				for(int i = 0; ( i < data.length ) && ( !found ); i++)	//for all of the node heads....
				{
					head = (Node<MapEntry<K,V>>) data[i];
					cur = head.next;
					
					while(( cur != null ) && ( !found ))
					{
						if(((MapEntry<K,V>)cur.data).getValue().equals(value))
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
		Node<MapEntry<K,V>> head = (HashMap<K, V>.Node<MapEntry<K,V>>) data[Math.floorMod(HashCode(key),data.length)];
		Node<MapEntry<K,V>> cur;
		boolean found = false;
		V output = null;
		
		if(this.containsKey(key))	//If the key exists in the structure...
		{
			cur = head.next;
			while(( cur != null ) && ( !found ))	//for all the items in the list and while it's not found...
			{
				if(((MapEntry<K, V>)cur.data).getKey().equals(key))
				{
					output = ((MapEntry<K, V>)cur.data).getValue();
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
		
		//TODO: Implement cases where it is an update or a standard put
		V output = this.get(key);	//Outputs the old value
		boolean update = false;
		
		Node<MapEntry<K,V>> head = (HashMap<K, V>.Node<MapEntry<K,V>>) data[Math.floorMod(HashCode(key),data.length)];
		Node<MapEntry<K,V>> cur = head;
		
		while(cur.next != null && !update)	//traverse the list until we are adding a node to the cur.next
		{
			cur = cur.next;
			
			if (cur.data.getKey().equals(key))
			{
				update = true;
			}
		}
		
		if(update)	//If it is an update...
		{
			cur.data.setValue(value);
		} else		//If it is not an update...
		{
			cur.next = new Node<MapEntry<K,V>>(new MapEntry<K,V>(key,value));
			size++;
		}
			
		return output;
	}

	@Override
	@SuppressWarnings("unchecked")
	public V remove(Object key) {
		V value = null;
		Node<MapEntry<K,V>> kill = null;
		if(this.containsKey(key)) //If this contains the key to remove...
		{
			Node<MapEntry<K,V>> head = (Node<MapEntry<K,V>>) data[Math.floorMod(HashCode(key),data.length)];
			Node<MapEntry<K,V>> cur = head;
			
			while(!((MapEntry<K, V>)cur.next.data).getKey().equals(key))	//traverse the list until we are just before the one to remove
				cur = cur.next;
			
			value = ((MapEntry<K, V>)cur.next.data).getValue();
			
			kill = cur.next;			//That is the one we want to obliterate
			cur.next = cur.next.next;	//set the curent node.next to the one after the removal one
			kill.next = null;			//Make sure that the one we are killing is not pointing to anything
			size--;
		}
		return value;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Map.Entry<? extends K, ? extends V> e: map.entrySet()) {
			this.put(e.getKey(), e.getValue());
		}
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<K> keySet() {
		return new AbstractSet<K>() {
			@Override
			public Iterator<K> iterator() {
				return new ViewIterator<K>() {
					@Override
					public K next() {
						return nextEntry().getKey();
					}
				};
			}

			@Override
			public int size() {
				return HashMap.this.size();
			}
		};
	}

	@Override
	public Collection<V> values() {
		return new AbstractCollection<V>() {
			@Override
			public Iterator<V> iterator() {
				return new ViewIterator<V>() {
					@Override
					public V next() {
						return nextEntry().getValue();
					}
				};
			}

			@Override
			public int size() {
				return HashMap.this.size();
			}
		};
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new AbstractSet<Map.Entry<K,V>>() {
			@Override
			public Iterator<Map.Entry<K, V>> iterator() {
				return new ViewIterator<Map.Entry<K, V>>() {
					@Override
					public Map.Entry<K, V> next() {
						return nextEntry();
					}
				};
			}

			@Override
			public int size() {
				return HashMap.this.size();
			}
		};
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
		Node<MapEntry<K, V>> head = null;
		Node<MapEntry<K, V>> cur = null;
		
		
		for(int i = 0; i < data.length; i++)
		{
			head = (Node<MapEntry<K, V>>) data[i];
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
	
	//ViewIterator class
	private abstract class ViewIterator<T> implements Iterator<T> {
		int index = 0;
		int itemsViewed = 0;
		@SuppressWarnings("unchecked")
		Node<MapEntry<K,V>> cur = (HashMap<K, V>.Node<MapEntry<K, V>>) data[0];
		
		@Override
		public boolean hasNext() {
			return itemsViewed < HashMap.this.size();
		}
		
		@SuppressWarnings("unchecked")
		protected MapEntry<K,V> nextEntry() {
			if(cur.next != null)	//If there is something left in the chain...
			{
				cur = cur.next;	
			} else 					//If we need to move on to the next chain
			{
				boolean found = false;
				while(!found)
				{
					index++;
					if(((Node<MapEntry<K,V>>)data[index]).next != null)
					{
						found = true;
						cur = ((Node<MapEntry<K,V>>)data[index]).next;
					}
				}
			}
			
			itemsViewed++;	//Bump up the items viewed
			return cur.data;
		}
	}
	
	//Entry Class
	class MapEntry<A,B> implements java.util.Map.Entry<A, B> {
		//Init Vars
		private A key = null;
		private B value = null;
		
		//Constructors
		public MapEntry(A key, B value)
		{
			this.key = key;
			this.value = value;
		}
		
		public MapEntry(A key)
		{
			this.key = key;
			this.value = null;
		}
		
		//Implemented methods
		@Override
		public A getKey() {
			return key;
		}

		@Override
		public B getValue() {
			return value;
		}

		@Override
		public B setValue(B value) {
			this.value = value;
			return value;
		}

	}

}