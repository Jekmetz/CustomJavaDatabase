package adt;

import java.util.Map;

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
	
	public HashMap() {
		super();
	}
	
	public HashMap(Map<? extends K, ? extends V> copy) {
		super(copy);
	}
}