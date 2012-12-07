package com.qorporation.qluster.async;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.service.Service;
import com.qorporation.qluster.service.ServiceManager;

public class AsyncService extends Service {
		
	private ExecutorService executor = null; 
	
	@Override
	public void init(ServiceManager serviceManager, Config config) {
		this.logger.info("Loading async service");
		
		setupPool(config);
	}
	
	private void setupPool(Config config) {
		this.executor = Executors.newCachedThreadPool();
	}
	
	public <T> Future<T> queue(AsyncTask<T> task) { return this.queue(null, task); }
	public <T> List<Future<T>> queue(AsyncTask<T> ... tasks) { return this.queue(null, tasks); }
	public <T> List<Future<T>> queue(List<AsyncTask<T>> tasks) { return this.queue(null, tasks); }
	
	public <T> Future<T> queue(AsyncTaskCostTracker costTracker, AsyncTask<T> task) { task.attachCostTracker(costTracker); this.executor.execute(task); return task.getFuture(); }
	public <T> List<Future<T>> queue(AsyncTaskCostTracker costTracker, AsyncTask<T> ... tasks) { return this.queue(costTracker, Arrays.asList(tasks)); }
	public <T> List<Future<T>> queue(AsyncTaskCostTracker costTracker, List<AsyncTask<T>> tasks) {
		List<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
		for (AsyncTask<T> task: tasks) {
			futures.add(this.queue(costTracker, task));
		}
		return futures;
	}
	
}
