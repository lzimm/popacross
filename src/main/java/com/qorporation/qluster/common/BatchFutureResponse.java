package com.qorporation.qluster.common;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BatchFutureResponse<T> implements Future<Set<T>> {

	private class PartialFutureResponse<P> extends FutureResponse<P> {
		public PartialFutureResponse() {
			this.latch = BatchFutureResponse.this.latch;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void set(P val) {
			BatchFutureResponse.this.add((Collection<T>) val);
			BatchFutureResponse.this.latch.countDown();
		}
	}
	
	protected CountDownLatch latch = null;
	protected ConcurrentSkipListSet<T> val = null;
	
	public BatchFutureResponse(int count) {
		this.latch = new CountDownLatch(count);
		this.val = new ConcurrentSkipListSet<T>();
	}

	public void add(Collection<T> vals) {
		this.val.addAll(vals);
		this.latch.countDown();
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public Set<T> get() throws InterruptedException, ExecutionException {
		this.latch.await();
		return this.val;
	}

	@Override
	public Set<T> get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		this.latch.await(timeout, unit);
		return this.val;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return this.latch.getCount() == 0;
	}

	public <P> FutureResponse<P> getComponentFuture(Class<P> cls) {
		return new PartialFutureResponse<P>();
	}

}
