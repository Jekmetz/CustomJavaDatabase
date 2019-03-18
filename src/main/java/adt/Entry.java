package adt;

public class Entry<K,V> implements java.util.Map.Entry<K, V> {
	//Init Vars
	private K key = null;
	private V value = null;
	
	//Constructors
	public Entry(K key, V value)
	{
		this.key = key;
		this.value = value;
	}
	
	public Entry(K key)
	{
		this.key = key;
		this.value = null;
	}
	
	//Implemented methods
	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		this.value = value;
		return value;
	}

}
