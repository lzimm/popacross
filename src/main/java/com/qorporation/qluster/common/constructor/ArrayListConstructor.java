package com.qorporation.qluster.common.constructor;

import java.util.ArrayList;
import java.util.List;

import com.qorporation.qluster.common.functional.F1;

public class ArrayListConstructor<K, T> implements F1<K, List<T>> {

	@Override
	public List<T> f(K a) {
		return new ArrayList<T>();
	}

}
