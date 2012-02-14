/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
 ******************************************************************************/


package com.linuxbox.enkive.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import com.linuxbox.enkive.mailprocessor.ThreadedProcessor;
import com.linuxbox.enkive.server.config.ThreadPoolServerConfiguration;
import com.linuxbox.util.threadpool.ThreadAspects;
import com.linuxbox.util.threadpool.ThreadPoolExecutorWithAspects;

/**
 * Represents a server that makes use of a ThreadPoolExecutor. Makes no
 * assumptions about what this server does (e.g., no notion of archiving).
 * 
 * @author eric
 * 
 */
public abstract class ThreadPoolServer extends AbstractSocketServer implements
		ThreadPoolServerMBean {
	protected ThreadPoolExecutor threadPool;
	protected final ArrayBlockingQueue<Runnable> queue;

	public ThreadPoolServer(String serverName, int port,
			ThreadPoolServerConfiguration poolConfig) {
		super(serverName, port, (poolConfig.getMaximumPoolSize() + (poolConfig
				.getMaximumPoolSize() / 2)));

		queue = new ArrayBlockingQueue<Runnable>(poolConfig.getQueueSize());

		ThreadAspects threadAspects = null;
		if (this instanceof ThreadAspects) {
			threadAspects = (ThreadAspects) this;
		}

		threadPool = new ThreadPoolExecutorWithAspects(
				poolConfig.getCorePoolSize(), poolConfig.getMaximumPoolSize(),
				poolConfig.getKeepAliveTime(), poolConfig.getTimeUnit(), queue,
				threadAspects);
	}

	@Override
	public void processorClosed(ThreadedProcessor processor) {
		// TODO: this is a notification that the processor was closed; does
		// anything need to be done here to manage that?
	}

	@Override
	protected void shutdownProcessors() {
		threadPool.shutdown();
	}

	/**
	 * ThreadPoolServerMBean
	 */
	@Override
	public int getEnqueuedCount() {
		return queue.size();
	}

	/**
	 * ThreadPoolServerMBean
	 */
	@Override
	public int getActiveCount() {
		return threadPool.getActiveCount();
	}

	/**
	 * ThreadPoolServerMBean
	 */
	@Override
	public long getTaskCount() {
		return threadPool.getTaskCount();
	}

	/**
	 * ThreadPoolServerMBean
	 */
	@Override
	public int getPoolSize() {
		return threadPool.getPoolSize();
	}

	/**
	 * ThreadPoolServerMBean
	 */
	@Override
	public int getCorePoolSize() {
		return threadPool.getCorePoolSize();
	}

	/**
	 * ThreadPoolServerMBean
	 */
	@Override
	public int getMaximumPoolSize() {
		return threadPool.getMaximumPoolSize();
	}

	/**
	 * ThreadPoolServerMBean
	 */
	@Override
	public int getLargestPoolSize() {
		return threadPool.getLargestPoolSize();
	}
}
