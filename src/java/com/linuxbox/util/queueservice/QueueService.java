package com.linuxbox.util.queueservice;

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
	 * the range defined by the server/server-count.
	 * 
	 * @param identifer
	 * @return
	 */
	QueueEntry dequeueByShardKey(int server, int serverCount)
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
