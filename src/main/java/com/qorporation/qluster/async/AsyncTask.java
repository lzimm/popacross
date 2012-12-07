package com.qorporation.qluster.async;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.qorporation.qluster.util.ErrorControl;

public abstract class AsyncTask<T> implements Runnable {
	private class AsyncTaskFuture implements Future<T> {
		private CountDownLatch latch = new CountDownLatch(1);
		private AtomicReference<T> val = new AtomicReference<T>();
		private AtomicInteger state = new AtomicInteger(0);
		@Override public boolean cancel(boolean mayInterruptIfRunning) { return this.state.compareAndSet(0, 2); }
		@Override public T get() throws InterruptedException, ExecutionException { latch.await(); return this.val.get(); }
		@Override public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException { latch.await(timeout, unit); return this.val.get(); }
		@Override public boolean isCancelled() { return this.state.intValue() == 2; }
		@Override public boolean isDone() { return this.latch.getCount() == 0; }
		public boolean start() { return this.state.compareAndSet(0, 1); }
		public void set(T val) { this.val.set(val); this.latch.countDown(); }
	}
	
	private AsyncTaskFuture future = new AsyncTaskFuture();
	public Future<T> getFuture() { return this.future; }
	
	private AsyncTaskCostTracker costTracker = null;
	public void attachCostTracker(AsyncTaskCostTracker costTracker) { this.costTracker = costTracker; }
	
	@Override 
	public void run() { 
		try {
			AsyncTaskCostTracker.IncrementalCheckout cost = new AsyncTaskCostTracker.IncrementalCheckout(this.getCost());
			if (this.costTracker != null) {
				while (!this.costTracker.checkout(cost)) {
					Thread.sleep(10);
				}
			}
			
			if (this.future.start()) {
				this.future.set(this.call()); 
			}
			
			if (this.costTracker != null) {
				this.costTracker.checkin(cost);
			}
		} catch (Exception e) { 
			ErrorControl.logException(e);
		}
	}
	
	public long getCost() { return 0l; }
	public abstract T call() throws Exception;

}
