package com.linuxbox.util.queueservice;

public interface QueueService {
	void startup() throws QueueServiceException;

	void shutdown() throws QueueServiceException;

	void enqueue(String identifier) throws QueueServiceException;

	void enqueue(String identifier, Object note) throws QueueServiceException;

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
	 * Tells the queueing system that a given entry has been completed and to
	 * mark it as completed (which may involve removing it).
	 * 
	 * @param item
	 */
	void finish(QueueEntry item) throws QueueServiceException;
}
