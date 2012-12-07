package com.qorporation.qluster.util;

import java.util.ArrayList;
import java.util.List;

import com.qorporation.qluster.common.Pair;

public class UnzipUtil {

	public static <A, B> Pair<List<A>, List<B>> unzip(List<Pair<A, B>> pairs) {
		return new Pair<List<A>, List<B>>(as(pairs), bs(pairs));
	}
	
	public static <A, B> List<A> as(List<Pair<A, B>> pairs) {
		List<A> as = new ArrayList<A>(pairs.size());
		
		for (Pair<A, B> pair: pairs) {
			as.add(pair.a());
		}
		
		return as;
	}
	
	public static <A, B> List<B> bs(List<Pair<A, B>> pairs) {
		List<B> bs = new ArrayList<B>(pairs.size());
		
		for (Pair<A, B> pair: pairs) {
			bs.add(pair.b());
		}
		
		return bs;
	}
	
}
