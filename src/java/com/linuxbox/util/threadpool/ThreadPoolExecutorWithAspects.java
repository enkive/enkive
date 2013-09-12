/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadPoolExecutorWithAspects extends ThreadPoolExecutor {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.mailprocessor");

	private ThreadAspects threadAspects;

	public ThreadPoolExecutorWithAspects(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadAspects threadAspects,
			String baseName) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new
				NamedThreadFactory(baseName));
		this.threadAspects = threadAspects;
	}

	public ThreadPoolExecutorWithAspects(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
			String baseName) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, null,
				baseName);
	}

	public ThreadPoolExecutorWithAspects(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, null,
				null);
	}

	@Override
	protected void beforeExecute(Thread thread, Runnable runnable) {
		if (threadAspects != null) {
			threadAspects.beforeExecute(thread, runnable);
		}
	}

	@Override
	protected void afterExecute(Runnable runnable, Throwable throwable) {
		if (threadAspects != null) {
			threadAspects.afterExecute(runnable, throwable);
		}
	}
}
