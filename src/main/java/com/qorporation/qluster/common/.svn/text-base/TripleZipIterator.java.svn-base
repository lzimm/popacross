package com.qorporation.qluster.common;

import java.util.Iterator;
import java.util.List;

public class TripleZipIterator<A, B, C> implements Iterator<Triple<A, B, C>>, Iterable<Triple<A, B, C>> {

	private Iterator<A> ai = null;
	private Iterator<B> bi = null;
	private Iterator<C> ci = null;
	
	public TripleZipIterator(List<A> as, List<B> bs, List<C> cs) {
		if (as.size() != bs.size() || bs.size() != cs.size()) {
			throw new Error(String.format("Cannot zip lists with mismatched lengths: %s %s %s", as.size(), bs.size(), cs.size()));
		}
		
		this.ai = as.iterator();
		this.bi = bs.iterator();
		this.ci = cs.iterator();
	}
	
	@Override
	public boolean hasNext() {
		return this.ai.hasNext();
	}

	@Override
	public Triple<A, B, C> next() {
		return new Triple<A, B, C>(this.ai.next(), this.bi.next(), this.ci.next());
	}

	@Override
	public void remove() {
		this.ai.remove();
		this.bi.remove();
		this.ci.remove();
	}

	@Override
	public Iterator<Triple<A, B, C>> iterator() {
		return this;
	}

}
