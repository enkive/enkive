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
 *******************************************************************************/
package com.linuxbox.util.queueservice;

/**
 * NOTE: this interface and subclasses should probably be generalized at some
 * point. It seems odd for the shard key to be at this level of generalization,
 * yet it will be a valid selection field. So I'm thinking there should be a
 * generalized query mechanism along the lines of what MongoDB offers.
 * 
 * @author eric
 * 
 */
public interface QueueService {
	void startup() throws QueueServiceException;

	void shutdown() throws QueueServiceException;

	void enqueue(String identifier) throws QueueServiceException;

	void enqueue(String identifier, int shardKey, Object note)
			throws QueueServiceException;

	/**
	 * Retrieves the next unstarted item on the queue and marks it as being
	 * started.
	 * 
	 * @return
	 */
	QueueEntry dequeue() throws QueueServiceException;

	/**
	 * Retrieves the next unstarted item on the queue with the specific
	 * identifier and marks it as being started.
	 * 
	 * @param identifer
	 * @return
	 */
	QueueEntry dequeue(String identifer) throws QueueServiceException;

	/**
	 * Retrieves the next unstarted item on the queue with a shard key within
	 * the range specified.
	 * 
	 * @param identifer
	 * @return
	 */
	QueueEntry dequeueByShardKey(int rangeLow, int rangeHigh)
			throws QueueServiceException;

	/**
	 * Tells the queueing system that a given entry has been completed and to
	 * mark it as completed (which may involve removing it).
	 * 
	 * @param item
	 */
	void finishEntry(QueueEntry item) throws QueueServiceException;

	/**
	 * Tells the queueing system that a given entry has been completed and to
	 * mark it as completed (which may involve removing it).
	 * 
	 * @param item
	 */
	void markEntryAsError(QueueEntry item) throws QueueServiceException;
}
