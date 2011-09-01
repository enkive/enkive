/*
 *  Copyright 2011 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.enkive.workspace;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.linuxbox.enkive.search.SearchProcess;
import com.linuxbox.util.MBeanUtils;
import com.linuxbox.util.threadpool.CancellableProcessExecutor;
import com.linuxbox.util.threadpool.CancellableProcessExecutor.CPFuture;

public abstract class AbstractWorkspaceService implements WorkspaceService {
	private final int DEFAULT_INTERACTIVE_SEARCH_TIMEOUT = 15;
	private final int DEFAULT_SEARCH_THREAD_COUNT = 5;

	private int interactiveSearchWaitSeconds;
	private int desiredSearchThreadCount;
	private CancellableProcessExecutor searchProcessExecutor;

	public AbstractWorkspaceService() {
		interactiveSearchWaitSeconds = DEFAULT_INTERACTIVE_SEARCH_TIMEOUT;
		desiredSearchThreadCount = DEFAULT_SEARCH_THREAD_COUNT;

		// using an unbounded queue
		final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
		searchProcessExecutor = new CancellableProcessExecutor(
				desiredSearchThreadCount, desiredSearchThreadCount, 1,
				TimeUnit.MINUTES, workQueue);
	}

	public void registerMBean() {
		final String type = getClass().getSimpleName();
		final String name = "the service";
		MBeanUtils.registerMBean(this, WorkspaceServiceMBean.class, type, name);
	}

	@Override
	public CPFuture<Set<String>> submitSearchProcessToQueue(
			SearchProcess process) {
		return searchProcessExecutor.submit(process);
	}

	@Override
	public void requestSearchCancellation(SearchResult result) {
		searchProcessExecutor.cancel(result.getId(), false);
	}

	@Override
	public int getInteractiveSearchWaitSeconds() {
		return interactiveSearchWaitSeconds;
	}

	@Override
	public void setInteractiveSearchWaitSeconds(int secondsToWait) {
		interactiveSearchWaitSeconds = secondsToWait;
	}

	@Override
	public int getDesiredSearchThreadCount() {
		return desiredSearchThreadCount;
	}

	@Override
	public int getActualSearchThreadCount() {
		return searchProcessExecutor.getPoolSize();
	}

	@Override
	public void setDesiredSearchThreadCount(int requestedSearchThreadCount) {
		if (requestedSearchThreadCount > this.desiredSearchThreadCount) {
			// growing thread count
			searchProcessExecutor
					.setMaximumPoolSize(requestedSearchThreadCount);
			searchProcessExecutor.setCorePoolSize(requestedSearchThreadCount);
		} else {
			// shrinking thread count
			searchProcessExecutor.setCorePoolSize(requestedSearchThreadCount);
			searchProcessExecutor
					.setMaximumPoolSize(requestedSearchThreadCount);
		}

		this.desiredSearchThreadCount = requestedSearchThreadCount;
	}
}
