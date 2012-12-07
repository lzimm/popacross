package com.qorporation.qluster.common;

import java.util.Iterator;
import java.util.List;

public class PairZipIterator<A, B> implements Iterator<Pair<A, B>>, Iterable<Pair<A, B>> {

	private Iterator<A> ai = null;
	private Iterator<B> bi = null;
	
	public PairZipIterator(List<A> as, List<B> bs) {
		if (as.size() != bs.size()) {
			throw new Error(String.format("Cannot zip lists with mismatched lengths: %s %s", as.size(), bs.size()));
		}
		
		this.ai = as.iterator();
		this.bi = bs.iterator();
	}
	
	@Override
	public boolean hasNext() {
		return this.ai.hasNext();
	}

	@Override
	public Pair<A, B> next() {
		return new Pair<A, B>(this.ai.next(), this.bi.next());
	}

	@Override
	public void remove() {
		this.ai.remove();
		this.bi.remove();
	}

	@Override
	public Iterator<Pair<A, B>> iterator() {
		return this;
	}

}
