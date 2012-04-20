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

import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.linuxbox.enkive.mailprocessor.AbstractMailProcessor;
import com.linuxbox.enkive.mailprocessor.ArchivingProcessor;
import com.linuxbox.enkive.mailprocessor.ThreadedProcessor;
import com.linuxbox.enkive.server.config.ThreadPoolServerConfiguration;
import com.linuxbox.util.threadpool.ThreadAspects;

/**
 * A ThreadPoolServer that does archiving.
 * 
 * @author eric
 * 
 */
public abstract class ArchivingThreadPoolServer extends ThreadPoolServer
		implements ArchivingThreadPoolServerMBean, ThreadAspects,
		ApplicationContextAware {
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.server");

	protected static ApplicationContext applicationContext = null;

	// private Set<AbstractMailProcessor> liveProcessors = Collections
	// .synchronizedSet(new HashSet<AbstractMailProcessor>());
	private Set<AbstractMailProcessor> liveProcessors = new HashSet<AbstractMailProcessor>();

	private int historicMessagesProcessed;
	private double historicMillisecondsPerMessage;

	public ArchivingThreadPoolServer(String serverName, int port,
			ThreadPoolServerConfiguration poolConfig) {
		super(serverName, port, poolConfig);
	}

	protected ThreadedProcessor initializeProcessor(Socket sessionSocket) {

		ArchivingProcessor processor = createArchivingProcessor();

		processor.initializeProcessor(this, sessionSocket);

		return processor;
	}

	@Override
	protected void createAndStartProcessor(Socket socket) throws Exception {
		// If there is space left in the queue, process the message.
		// Otherwise, reject the message and let the client end manage it.
		try {
			ThreadedProcessor session = initializeProcessor(socket);
			threadPool.execute(session);
		} catch (RejectedExecutionException e) {
			socket.close();
		}
	}

	abstract protected ArchivingProcessor createArchivingProcessor();

	/**
	 * Does not incorporate data from currently running processes. Maybe this
	 * should change in the future.
	 */
	@Override
	public int getMessagesProcessed() {
		return historicMessagesProcessed;
	}

	/**
	 * Does not incorporate data from currently running processes. Maybe this
	 * should change in the future.
	 */
	@Override
	public double getMillisecondsPerMessage() {
		return historicMillisecondsPerMessage;
	}

	/**
	 * When a new thread starts, add it to our own internal collection, so we
	 * have the opportunity to query it as it's running.
	 */
	@Override
	public void beforeExecute(Thread thread, Runnable runnable) {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("in beforeExecute for "
					+ runnable.getClass().getName());
		if (runnable instanceof AbstractMailProcessor) {
			final AbstractMailProcessor processor = (AbstractMailProcessor) runnable;
			synchronized (liveProcessors) {
				liveProcessors.add(processor);
			}
		}
	}

	/**
	 * When a running thread finishes, remove it from our own internal
	 * collection, and incorporate it's data to the historic data.
	 */
	@Override
	public void afterExecute(Runnable runnable, Throwable throwable) {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("in afterExecute for " + runnable.getClass().getName());
		if (runnable instanceof AbstractMailProcessor) {
			final AbstractMailProcessor processor = (AbstractMailProcessor) runnable;
			synchronized (liveProcessors) {
				liveProcessors.remove(processor);
				final int threadMessagesProcessed = processor
						.getMessagesProcessed();
				historicMillisecondsPerMessage = (historicMillisecondsPerMessage
						* historicMessagesProcessed + processor
						.getMillisecondsPerMessage() * threadMessagesProcessed)
						/ (historicMessagesProcessed + threadMessagesProcessed);
				historicMessagesProcessed += threadMessagesProcessed;
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		PostfixFilterServer.applicationContext = ctx;

	}
}
