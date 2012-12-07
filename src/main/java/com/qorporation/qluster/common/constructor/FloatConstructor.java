package com.qorporation.qluster.common.constructor;

import com.qorporation.qluster.common.functional.F1;

public class FloatConstructor<K> implements F1<K, Float> {

	@Override
	public Float f(K a) {
		return new Float(0);
	}

}
