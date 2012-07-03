package com.linuxbox.enkive.statistics.services;

import java.util.Map;
import java.util.Set;

import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;

public interface StatsStorageService {
	void storeStatistics(Set<Map<String, Object>> dataSet)
			throws StatsStorageException;

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
	void storeStatistics(String service, Map<String, Object> data)
			throws StatsStorageException;
}
