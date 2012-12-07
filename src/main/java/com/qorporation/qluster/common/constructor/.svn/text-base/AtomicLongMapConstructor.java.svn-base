package com.qorporation.qluster.common.constructor;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.qorporation.qluster.common.ConstructingHashMap;
import com.qorporation.qluster.common.functional.F1;

public class AtomicLongMapConstructor<T, K> implements F1<T, Map<K, AtomicLong>> {

	@Override
	public Map<K, AtomicLong> f(T a) {
		return new ConstructingHashMap<K, AtomicLong>(new AtomicLongConstructor<K>());
	}

}
