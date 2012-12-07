package com.qorporation.qluster.common.functional;

import java.util.ArrayList;
import java.util.List;

public abstract class Mappable<A, R> implements F1<A, R> {

	public List<R> map(List<A> col) {
		List<R> ret = new ArrayList<R>(col.size());
		for (A el: col) {
			R val = this.f(el);
			if (val != null) {
				ret.add(val);
			}
		}
		
		return ret;
	}
	
}
