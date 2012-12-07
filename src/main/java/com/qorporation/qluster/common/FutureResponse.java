package com.qorporation.qluster.common;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class FutureResponse<T> implements Future<T> {

	protected CountDownLatch latch = null;
	protected AtomicReference<T> val = null;
	
	public FutureResponse() {
		this.latch = new CountDownLatch(1);
		this.val = new AtomicReference<T>(null);
	}

	public void set(T val) {
		this.val.set(val);
		this.latch.countDown();
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		this.latch.await();
		return this.val.get();
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		this.latch.await(timeout, unit);
		return this.val.get();
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return this.latch.getCount() == 0;
	}
	
}
