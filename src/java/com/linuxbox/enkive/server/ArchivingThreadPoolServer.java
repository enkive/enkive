/*
 *  Copyright 2010 The Linux Box Corporation.
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

package com.linuxbox.enkive.server;

import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.archiver.MessageArchivingService;
import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.server.config.ThreadPoolServerConfiguration;
import com.linuxbox.enkive.mailprocessor.AbstractMailProcessor;
import com.linuxbox.enkive.mailprocessor.ArchivingProcessor;
import com.linuxbox.enkive.mailprocessor.ThreadedProcessor;
import com.linuxbox.util.threadpool.ThreadAspects;

/**
 * A ThreadPoolServer that does archiving.
 * 
 * @author eric
 * 
 */
public abstract class ArchivingThreadPoolServer extends ThreadPoolServer
		implements ArchivingThreadPoolServerMBean, ThreadAspects {
	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.server");

	protected MessageArchivingService archiver;
	protected AuditService auditService;
	
	// private Set<AbstractMailProcessor> liveProcessors = Collections
	// .synchronizedSet(new HashSet<AbstractMailProcessor>());
	private Set<AbstractMailProcessor> liveProcessors = new HashSet<AbstractMailProcessor>();

	private int historicMessagesProcessed;
	private double historicMillisecondsPerMessage;

	public ArchivingThreadPoolServer(MessageArchivingService archiver, AuditService auditService, String serverName, int port,
			ThreadPoolServerConfiguration poolConfig) {
		super(serverName, port, poolConfig);
		this.archiver = archiver;
		this.auditService = auditService;
	}

	protected ThreadedProcessor initializeProcessor(Socket sessionSocket, MessageArchivingService archiver){

		ArchivingProcessor processor = createArchivingProcessor();

		processor.initializeProcessor(this, sessionSocket, archiver, auditService);

		return processor;
	}

	@Override
	protected void createAndStartProcessor(Socket socket) throws Exception {
		// If there is space left in the queue, process the message.
		// Otherwise, reject the message and let the client end manage it.
		try {
			ThreadedProcessor session = initializeProcessor(socket, archiver);
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
		logger.trace("in beforeExecute for " + runnable.getClass().getName());
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
		logger.trace("in afterExecute for " + runnable.getClass().getName());
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
}
