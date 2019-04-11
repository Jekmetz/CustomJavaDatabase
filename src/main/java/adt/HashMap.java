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
public class HashMap<K,V> implements Map<K,V>{
	//Initialize Vars
	//private static final long serialVersionUID = 1L;
	private final int INITIAL_SIZE = 11;	//Initial size of Map
	private int size = 0, alpha = 0;		//Hard coded size and Alpha
	private Object[] data = null;			//data object
	private int[] arrLengths = {23,59,127,257,521,1049,2111,4229,8461,16879,33023,66271,104729,208049,416159,832189,1299827};
	private int arrLengthIndex = 0;			//index deciding what prime resize length we are on
	boolean doAResizeMate = false;			//flag determining whether or not to resize
	//Constructors
	/**
	 * Constructor that creates new Node array with size INITIAL_SIZE
	 */
	public HashMap() {
		data = new Object[INITIAL_SIZE];
		for(int i = 0; i < data.length; i++)							//For every element in data...
			data[i] = new Node<MapEntry<K,V>>(new MapEntry<K,V>(null));	//Add a head node to it.
		
		//TODO: May have to just default as null and in the put method, if the slot is null, then put a head node in it. The post-ceding code after that is logically correct!
	
		size = 0;	//Redundant but helpful to see that the size is 0
	}
	/**
	 * Constructor that takes a map and puts all the elements in this HashMap
	 */
	public HashMap(Map<? extends K,? extends V> map) {
		this();
		this.putAll(map);
	}
	
	
	//Implemented methods
	@Override
	public int size() { return size; }

	@Override
	public boolean isEmpty() { return size == 0;}

	@Override
	@SuppressWarnings("unchecked")
	public boolean containsKey(Object key) {
		Node<MapEntry<K,V>> head = (Node<MapEntry<K,V>>)data[Math.floorMod(localHash(key),data.length)];	//Head is the data element that will be accepting the new Entry
		Node<MapEntry<K,V>> cur = null;	//Initialize cur
		boolean found = false;			//flag to check if the key has been found
		
		cur = head.next;				//set cur to the next element past the head
		while(( cur != null ) && ( !found ))	//while we are not at the end of the list and we have not found it...
		{
			if(((MapEntry<K,V>)cur.data).getKey().equals(key))	//If the key is in that position...
				found = true;
			
			cur = cur.next;	//increment the cur. If it was found, then it will be the one after the found key but that's okay because we don't have to use it!
		}
		
		return found;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean containsValue(Object value) {
		boolean found = false;
		
		if(this.size() != 0)	//If the array is not empty...
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
		Node<MapEntry<K,V>> head = (Node<MapEntry<K,V>>) data[Math.floorMod(localHash(key),data.length)]; //set the head to the element in which that object will fall...
		Node<MapEntry<K,V>> cur;
		boolean found = false;
		V output = null;
		
		if(this.containsKey(key))	//If the key exists in the structure...	//could write the code for this myself again and modify to fit my needs but it is constant time so I don't care!
		{
			cur = head.next;
			while(( cur != null ) && ( !found ))	//for all the items in the list and while it's not found...
			{
				if(((MapEntry<K, V>)cur.data).getKey().equals(key))	//If we have found the node that has this key...
				{
					output = ((MapEntry<K, V>)cur.data).getValue();	//give it to me!
					found = true;									//Stop the loop while you're at it!
				}
				
				cur = cur.next;	//Increment cur
			}
		}
		
		return output;
	}

	@Override
	@SuppressWarnings("unchecked")
	public V put(K key, V value) {
		
		if(doAResizeMate)	//If we should do a resize...
			resize();		//resize!
		
		V output = this.get(key);	//Outputs the old value
		boolean update = false;		//is it an update?
		
		Node<MapEntry<K,V>> head = (Node<MapEntry<K,V>>) data[Math.floorMod(localHash(key),data.length)];
		Node<MapEntry<K,V>> cur = head;
		
		while(cur.next != null && !update)	//traverse the list until we are adding a node to the cur.next or we have determined that it is an update!
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
		
		doAResizeMate = calculateAlpha() >= 5; //Calculate alpha every time
			
		return output;
	}

	@Override
	@SuppressWarnings("unchecked")
	public V remove(Object key) {
		V value = null;
		Node<MapEntry<K,V>> kill = null;
		if(this.containsKey(key)) //If this contains the key to remove...
		{
			Node<MapEntry<K,V>> head = (Node<MapEntry<K,V>>) data[Math.floorMod(localHash(key),data.length)];
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
		data = new Object[INITIAL_SIZE];
		for(int i = 0; i < data.length; i++)	//Just replace everything with a head
			data[i] = new Node<MapEntry<K,V>>(new MapEntry<K,V>(null));
		
		//ReInitialize Variables
		size = 0;
		alpha = 0;
		arrLengthIndex = 0;
		doAResizeMate = false;
	}

	@Override
	public Set<K> keySet() {
		return new AbstractSet<K>() {				//Abstract set needs iterator()
			@Override
			public Iterator<K> iterator() {			//iterator() returns a ViewIterator	
				return new ViewIterator<K>() {		//ViewIterator needs to define the next() method
					@Override
					public K next() {
						return nextEntry().getKey();//next() returns the key
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
	private int localHash(Object obj)
	{
		if(obj == null) return 0;
		
		if(obj.getClass().getSimpleName().equals("String"))	//If it is a string...
		{
			String str = (String) obj;						//cast obj to string...
			Random random = null;
			double seed = 0;
			
			for(int i = 0; i < str.length(); i++)					//for the length of the string...
				seed += Math.pow(str.charAt(i),str.length() - i);	//add the reversing index'th power of the ascii value of the character to the seed
			
			random = new Random((int)(seed % Integer.MAX_VALUE));	//Use that seed for the random number generator
			return (int) random.nextLong();							//cast random long to int
		} else	//If it is not a string
		{
			return obj.hashCode();
		}
	}
	
	//Utility functions
	private int calculateAlpha()
	{
		alpha = size/data.length;	//Mutate alpha
		return alpha;				//return that value
	}
	
	@SuppressWarnings("unchecked")
	private void resize()
	{
		doAResizeMate = false;	//Set this global variable to false to not do another resize until next time
		Object[] temp = data;
		data = (arrLengthIndex < arrLengths.length) ? new Object[arrLengths[arrLengthIndex]] : new Object[data.length*2+1];
		arrLengthIndex++;
		
		//initialize data again...
		for(int i = 0; i < data.length; i++)
			data[i] = new Node<MapEntry<K,V>>(new MapEntry<K,V>(null));
		
		//Reset the size because we are going to rehash and put everything back...
		size = 0;
		
		{
			Node<MapEntry<K,V>> cur = null;
			for(int i = 0; i < temp.length; i++) 		//for every chain in the array...
			{
				cur = (Node<MapEntry<K, V>>) temp[i];	//cur is the next head node
				
				while (cur.next != null)	//for every element in the chain...
				{
					cur = cur.next;
					this.put(cur.data.key,cur.data.value);
				}
			}
		}
	}
	
	public int getAlpha() { return alpha; }
	
	//Object Overrides
	@SuppressWarnings("unchecked")
	@Override
	public String toString()
	{
		String output = "{";
		Node<MapEntry<K, V>> head = null;
		Node<MapEntry<K, V>> cur = null;
		
		
		for(int i = 0; i < data.length; i++)		//for all the indexes in data
		{
			head = (Node<MapEntry<K, V>>) data[i];	//set the head for that element
			cur = head.next;						//set cur to the first good one
			
			if(cur != null)	//If the first good element is not null...
			{
				while(cur != null)	//While we have not reached the end of the chain
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
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object a)
	{
		boolean output = false; //assume it to be false and prove it is true
		
		if(a instanceof adt.HashMap)	//If a is an adt.HashMap...
		{
			adt.HashMap<K, V> copy = (adt.HashMap<K,V>)a; //If it has made it here, then it is an adt.HashMap
			
			if(this.size() == copy.size())	//If the sizes are the same...
			{
				ViewIterator<MapEntry<K,V>> thisItr = this.viewIterator();
				ViewIterator<MapEntry<K,V>> copyItr = copy.viewIterator();
				
				output = true; //set output to true until proven false at this point
				
				while (thisItr.hasNext() && output)
				{
					output = output && thisItr.next().equals(copyItr.next());	//make sure that all of the entries are the same
				}
				
			}
		}
		
		return output;
	}
	
	@Override
	public int hashCode()
	{
		int output = 0;
		for(Map.Entry<K, V> e : this.entrySet())
		{
			output += e.hashCode();
		}
		return output;
	}
	
	//Helpers
	private ViewIterator<MapEntry<K,V>> viewIterator()
	{
		return new ViewIterator<MapEntry<K,V>>() 
		{
			public MapEntry<K,V> next()
			{
				return nextEntry();
			}
		};
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
			return "D: " + data + "\nREF: " + next;
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
			MapEntry<K,V> output = null;
			if(index < data.length)	//If we have the ability to search
			{
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
				output = cur.data;
			} else 	//If we do not have the ability to search
			{
				output = null;
			}
			
			itemsViewed++;	//Bump up the items viewed
			return output;
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
		public A getKey() { return key; }

		@Override
		public B getValue() { return value;	}

		@Override
		public B setValue(B value) {
			this.value = value;
			return value;
		}
		
		@Override 
		public String toString()
		{
			return "<" + this.key + "," + this.value + ">";
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object a)
		{
			boolean output = false; //Assume it to be false
			
			if(a instanceof MapEntry)
			{
				MapEntry<A,B> copy = (MapEntry<A,B>) a;
				output = this.key.equals(copy.key) && this.value.equals(copy.value);
			}
			
			return output;
		}
		
		@Override
		public int hashCode()
		{
			return (this.getKey()==null   ? 0 : localHash(this.getKey())) ^ (this.getValue()==null ? 0 : localHash(this.getValue()));
		}

	}

}