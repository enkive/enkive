package com.linuxbox.enkive.statistics.storage;

import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 * NOAH: I don't know if we want to have statistics w/ different
 * granularity -- hourly, daily, monthly, annually. I created a
 * Granularity enum (the forgotten sibling of class and interface)
 * to encapsulate the idea and some of the logic. We want to think
 * in terms of cleaning up statistics every so often, so as to not
 * require too much storage. The three of us should talk this over.
 * -Eric
 */

public interface StatsStorageService {
	/**
	 * Store some statistics in the back-end that were collected by a specific
	 * service at a particular instance in time.
	 * 
	 * @param service
	 * @param timestamp
	 *            This should be the idealized
	 * @param data
	 * @throws StatsStorageException
	 */
	void storeStatistics(String service, Date timestamp,
			Map<String, Object> data) throws StatsStorageException;

	/**
	 * Does a query on the statistics service back end.
	 * 
	 * @param statName
	 *            A dot-separated path to the statistic desired starting with
	 *            the service name (e.g., "MessageStorageService.count",
	 *            "AuditService.count.max")
	 * @param startingTimestamp
	 * @param endingTimestamp
	 * @return a list of results. Each result could be a simple object (e.g.,
	 *         String, Integer) or something more complex (List, Map).
	 * @throws StatsStorageException
	 */
	List<Object> queryStatistics(String statName, Date startingTimestamp,
			Date endingTimestamp) throws StatsStorageException;
}
