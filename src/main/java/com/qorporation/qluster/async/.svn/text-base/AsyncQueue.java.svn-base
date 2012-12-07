package com.qorporation.qluster.async;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.qorporation.qluster.util.ErrorControl;

public class AsyncQueue {

	private ExecutorService executor = null;
	private AsyncTaskCostTracker costTracker = null;
	
	public AsyncQueue() { this(0l); }
	public AsyncQueue(long maxCost) {
		this.executor = Executors.newCachedThreadPool();
		this.costTracker = new AsyncTaskCostTracker(maxCost);
	}
	
	public <T> Future<T> call(AsyncTask<T> task) { try { task.call(); } catch (Exception e) { ErrorControl.logException(e); } return task.getFuture(); }
	public <T> Future<T> queue(AsyncTask<T> task, boolean async) { if (async) { return this.queue(task); } else { return this.call(task); } }
	public <T> Future<T> queue(AsyncTask<T> task) { task.attachCostTracker(this.costTracker); this.executor.execute(task); return task.getFuture(); }
	public <T> List<Future<T>> queue(AsyncTask<T> ... tasks) { return this.queue(Arrays.asList(tasks)); }
	public <T> List<Future<T>> queue(List<AsyncTask<T>> tasks) {
		List<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
		for (AsyncTask<T> task: tasks) {
			futures.add(this.queue(task));
		}
		return futures;
	}
	
}
