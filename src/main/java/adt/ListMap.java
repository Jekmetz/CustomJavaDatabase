package adt;

import java.util.Map;
import java.util.AbstractMap;
import java.util.Set;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.AbstractCollection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/** 
 * This is a naive partial map implementation based on a
 * list. It will be covered as part of a lab activity.
 * You are permitted to use any code from this class
 * when implementing a compliant map for the Hash modules.
 */
@Deprecated
public class ListMap<K,V> implements Map<K,V> {
	private List<Map.Entry<K,V>> list;
	
	public ListMap() {
		super();
		list = new ArrayList<Map.Entry<K,V>>();
	}
	
	public ListMap(Map<? extends K,? extends V> map) {
		this();
		this.putAll(map);
	}
	
	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		for (Map.Entry<K,V> e: list) {
			if (key == null ? e.getKey() == null : key.equals(e.getKey()))
				return true;
		}
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		for (Map.Entry<K,V> e: list) {
			if (value == null ? e.getValue() == null : value.equals(e.getValue()))
				return true;
		}
		return false;
	}

	@Override
	public V get(Object key) {
		for (Map.Entry<K,V> e: list) {
			if (key == null ? e.getKey() == null : key.equals(e.getKey()))
				return e.getValue();
		}
		return null;
	}
	
	@Override
	public V put(K key, V value) {
		for (Map.Entry<K,V> e: list) {
			if (key == null ? e.getKey() == null : key.equals(e.getKey())) {
				V before = e.getValue();
				e.setValue(value);
				return before;
			}
		}
		// TODO: implement Map.Entry using inner class
		Map.Entry<K,V> make = new AbstractMap.SimpleEntry<K,V>(key, value);
		list.add(make);
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Map.Entry<? extends K, ? extends V> e: map.entrySet()) {
			this.put(e.getKey(), e.getValue());
		}
	}

	@Override
	public V remove(Object key) {
		Iterator<Map.Entry<K,V>> iter = list.iterator();
		while (iter.hasNext()) {
			Map.Entry<K,V> e = iter.next();
			if (key == null ? e.getKey() == null : key.equals(e.getKey())) {
				V before = e.getValue();
				iter.remove();
				return before;
			}
		}
		return null;
	}
	
	private abstract class ViewIterator<T> implements Iterator<T> {
		int index = 0;
		
		@Override
		public boolean hasNext() {
			return index < list.size();
		}
		
		protected Map.Entry<K,V> nextEntry() {
			return list.get(index++);
		}
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
				return ListMap.this.size();
			}
		};
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
				return ListMap.this.size();
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
				return ListMap.this.size();
			}
		};
	}
	
	// TODO: implement equals method based on the API documentation

	// TODO: implement hashCode method based on the API documentation
	
	// TODO: implement toString method based on the API documentation
}
