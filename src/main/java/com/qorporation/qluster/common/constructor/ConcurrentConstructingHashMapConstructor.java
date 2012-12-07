package com.qorporation.qluster.common.constructor;

import com.qorporation.qluster.common.ConcurrentConstructingHashMap;
import com.qorporation.qluster.common.functional.F1;

public class ConcurrentConstructingHashMapConstructor<K, T, V> implements F1<K, ConcurrentConstructingHashMap<T, V>> {

	private F1<T, V> constructor;
	
	public ConcurrentConstructingHashMapConstructor(F1<T, V> constructor) {
		this.constructor = constructor;
	}
	
	@Override
	public ConcurrentConstructingHashMap<T, V> f(K a) {
		return new ConcurrentConstructingHashMap<T, V>(this.constructor);
	}

}
