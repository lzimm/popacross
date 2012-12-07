package com.qorporation.qluster.common;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureBatch implements Future<Void> {

	private List<Future<?>> futures = null;

	public FutureBatch() {
		this.futures = new LinkedList<Future<?>>();
	}

	public void add(Future<?> future) { this.futures.add(future); }
	public void add(Future<?> ... futures) { this.add(Arrays.asList(futures)); }
	public void add(List<Future<?>> futures) {
		for (Future<?> future: futures) {
			this.add(future);
		}
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public Void get() throws InterruptedException, ExecutionException {
		for (Future<?> future: this.futures) {
			future.get();
		}
		
		return null;
	}

	@Override
	public Void get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		for (Future<?> future: this.futures) {
			future.get(timeout, unit);
		}
		
		return null;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		for (Future<?> future: this.futures) {
			if (!future.isDone()) {
				return false;
			}
		}
		
		return true;
	}

}
