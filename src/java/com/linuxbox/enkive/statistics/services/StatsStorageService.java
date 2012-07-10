package com.linuxbox.enkive.statistics.services;

import java.util.Map;
import java.util.Set;

import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;

public interface StatsStorageService {
	
	/**
	 * Store all statistics contained within a set. The format of each map 
	 * is retained when it is stored.
	 * 
	 * @param dataSet
	 * @throws StatsStorageException
	 */
	void storeStatistics(Set<Map<String, Object>> dataSet)
			throws StatsStorageException;

	/**
	 * Store a map after appending a service name to it
	 * 
	 * @param service -- gatherer name
	 * @param data -- map to store
	 * @throws StatsStorageException
	 */
	void storeStatistics(String service, Map<String, Object> data)
			throws StatsStorageException;
}
