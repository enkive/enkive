/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
 *
 * This file is part of Enkive CE (Community Edition).
 *
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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

	public CancellableProcessExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		searches = new HashMap<String, Future<?>>();
		runnableMap = new HashMap<Runnable, String>();
	}

	public Future<?> submit(String searchId, Callable<?> task) {
		Future<?> searchFuture = super.submit(task);
		searches.put(searchId, searchFuture);
		if (searchFuture instanceof Runnable)
			runnableMap.put((Runnable) searchFuture, searchId);
		return searchFuture;
	}

	public boolean cancelSearch(String searchId) {
		Future<?> searchResult = searches.get(searchId);
		searches.remove(searchId);
		super.purge();
		return searchResult.cancel(true);
	}

	protected void afterExecute(Runnable r, Throwable t) {
		String searchId = runnableMap.get(r);
		runnableMap.remove(r);
		searches.remove(searchId);
		super.afterExecute(r, t);
	}
}
