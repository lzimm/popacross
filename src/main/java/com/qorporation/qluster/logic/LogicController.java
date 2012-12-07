package com.qorporation.qluster.logic;

import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.qluster.async.AsyncService;
import com.qorporation.qluster.async.AsyncTask;
import com.qorporation.qluster.service.ServiceManager;
import com.qorporation.qluster.util.ErrorControl;

public class LogicController {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected ServiceManager serviceManager = null;
	protected LogicService logicService = null;
	protected AsyncService asyncService = null;
	
	public void setup(ServiceManager serviceManager, LogicService logicService) {
		this.serviceManager = serviceManager;
		this.logicService = logicService;
		this.asyncService = logicService.getAsyncService();
	}

	public void init() {
	}
	
	public <T> Future<T> call(AsyncTask<T> task) { try { task.call(); } catch (Exception e) { ErrorControl.logException(e); } return task.getFuture(); }
	public <T> Future<T> queue(AsyncTask<T> task, boolean async) { if (async) { return this.queue(task); } else { return this.call(task); } }
	public <T> Future<T> queue(AsyncTask<T> task) { return this.asyncService.queue(task); }
	public <T> List<Future<T>> queue(AsyncTask<T> ... tasks) { return this.asyncService.queue(tasks); }
	public <T> List<Future<T>> queue(List<AsyncTask<T>> tasks) { return this.asyncService.queue(tasks); }
	
}
