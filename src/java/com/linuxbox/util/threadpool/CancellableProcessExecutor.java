package com.linuxbox.util.threadpool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CancellableProcessExecutor extends ThreadPoolExecutor {
	
	private Map<String, Future<?>> searches;
	private Map<Runnable, String> runnableMap;
	
	public CancellableProcessExecutor(int corePoolSize,
			int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		searches = new HashMap<String, Future<?>>();
		runnableMap = new HashMap<Runnable, String>();
	}
	
	public Future<?> submit(String searchId, Callable<?> task){
		Future<?> searchFuture = super.submit(task);
		searches.put(searchId, searchFuture);
		if(searchFuture instanceof Runnable)
			runnableMap.put((Runnable) searchFuture, searchId); 
		return searchFuture;
	}
	
	public boolean cancelSearch(String searchId){
		Future<?> searchResult = searches.get(searchId);
		searches.remove(searchId);
		super.purge();
		return searchResult.cancel(true);
	}

	protected void afterExecute(Runnable r, Throwable t){
		String searchId = runnableMap.get(r);
		runnableMap.remove(r);
		searches.remove(searchId);
		super.afterExecute(r, t);
	}
}
