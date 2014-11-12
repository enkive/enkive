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
package com.linuxbox.util.queueservice.mongodb;

import static com.linuxbox.enkive.docstore.DocStoreConstants.QUEUE_ENTRY_INDEX_DOCUMENT;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.util.queueservice.AbstractQueueEntry;
import com.linuxbox.util.queueservice.QueueEntry;
import com.linuxbox.util.queueservice.QueueService;
import com.linuxbox.util.queueservice.QueueServiceException;

public class JavaQueueService implements QueueService {
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.util.queueservice.mongodb");

	private LinkedHashMap<String, LinkedList<QueueEntry>> queue;
	private DocStoreService docStoreService;
	private LinkedList<QueueEntry> dequeued;

	public JavaQueueService() {
		queue = new LinkedHashMap<String, LinkedList<QueueEntry>>();
		dequeued = new LinkedList<QueueEntry>();
	}

	public void setDocStoreService(DocStoreService service) {
		this.docStoreService = service;
	}

	/**
	 * Pre-fill our queue with any messages that have not been indexed yet.
	 */
	@Override
	public void startup() throws QueueServiceException {
		String identifier;
		while ((identifier = docStoreService.nextUnindexed()) != null) {
			enqueue(identifier, -1, QUEUE_ENTRY_INDEX_DOCUMENT);
		}
	}

	@Override
	public void shutdown() throws QueueServiceException {
		// empty
	}

	@Override
	public void enqueue(String identifier) throws QueueServiceException {
		enqueue(identifier, -1, null);
	}

	@Override
	public synchronized void enqueue(String identifier, int shardKey, Object note)
			throws QueueServiceException {
		QueueEntry entry = new AbstractQueueEntry(new Date(), identifier, note, shardKey);
		LinkedList<QueueEntry> list = queue.get(identifier);
		if (list == null) {
			list = new LinkedList<QueueEntry>();
			queue.put(identifier, list);
		}
		list.addLast(entry);
	}

	@Override
	public synchronized QueueEntry dequeue() throws QueueServiceException {
		String identifier;
		try {
			identifier = queue.keySet().iterator().next();
		} catch (NoSuchElementException e) {
			return (null);
		}

		return (dequeue(identifier));
	}

	@Override
	public synchronized QueueEntry dequeue(String identifier) throws QueueServiceException {
		LinkedList<QueueEntry> list = queue.get(identifier);
		if (list == null) {
			return null;
		}
		QueueEntry entry = list.removeFirst();
		if (list.peek() == null) {
			queue.remove(identifier);
		}
		dequeued.push(entry);
		return (entry);
	}

	@Override
	public QueueEntry dequeueByShardKey(int rangeLow, int rangeHigh)
			throws QueueServiceException {
		return (this.dequeue());
	}

	@Override
	public void finishEntry(QueueEntry entry) throws QueueServiceException {
		if (!dequeued.remove(entry)) {
			throw new QueueServiceException("No dequeued entry");
		}
	}

	@Override
	public void markEntryAsError(QueueEntry entry) throws QueueServiceException {
		if (!dequeued.remove(entry)) {
			throw new QueueServiceException("No dequeued entry");
		}
	}
}
