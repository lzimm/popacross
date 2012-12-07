package com.qorporation.qluster.common.constructor;

import java.util.concurrent.ConcurrentHashMap;

import com.qorporation.qluster.common.functional.F1;

public class ConcurrentHashMapConstructor<K, T, V> implements F1<K, ConcurrentHashMap<T, V>> {

	@Override
	public ConcurrentHashMap<T, V> f(K a) {
		return new ConcurrentHashMap<T, V>();
	}

}
