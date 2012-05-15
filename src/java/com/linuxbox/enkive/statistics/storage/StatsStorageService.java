package com.linuxbox.enkive.statistics.storage;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface StatsStorageService {
	void storeStatistics(String service, Date timestamp,
			Map<String, Object> data) throws StatsStorageException;

	/**
	 * Does a query on the statistics service back end.
	 * 
	 * @param statName A dot-separated path to the statistic desired starting with the service name (e.g., "MessageStorageService.count", "AuditService.count.max")
	 * @param startingTimestamp
	 * @param endingTimestamp
	 * @return a list of results. Each result could be a simple object (e.g., String, Integer) or something more complex (List, Map).
	 * @throws StatsStorageException
	 */
	List<Object> queryStatistics(String statName, Date startingTimestamp,
			Date endingTimestamp) throws StatsStorageException;
}
