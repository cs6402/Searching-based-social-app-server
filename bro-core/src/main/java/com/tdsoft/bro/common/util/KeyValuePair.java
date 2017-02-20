package com.tdsoft.bro.common.util;

import com.google.common.base.MoreObjects;

public class KeyValuePair<K, V> {
	K key;
	V value;

	public K getKey() {
		return key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Key", getKey()).add("Value", getValue()).toString();
	}
}
