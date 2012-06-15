package com.linuxbox.enkive.statistics.services;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.linuxbox.enkive.statistics.retrieval.StatsRetrievalException;
import com.linuxbox.enkive.statistics.storage.StatsStorageException;

public interface StatsRetrievalService {
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
	public Set<Map<String, Object>> queryStatistics()
			throws StatsRetrievalException;

	public Set<Map<String, Object>> queryStatistics(Map<String, Map<String, Object>> stats)
			throws StatsRetrievalException;

	public Set<Map<String, Object>> queryStatistics(Date startingTimestamp,
			Date endingTimestamp) throws StatsRetrievalException;

	public Set<Map<String, Object>> queryStatistics(
			Map<String, Map<String, Object>> stats, Date startingTimestamp,
			Date endingTimestamp) throws StatsRetrievalException;
	
	public void remove(Set<Object> deletionSet) throws StatsRetrievalException;
}
