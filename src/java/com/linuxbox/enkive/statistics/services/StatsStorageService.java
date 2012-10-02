package com.linuxbox.enkive.statistics.services;

import java.util.List;
import java.util.Map;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;

public interface StatsStorageService {
	
	/**
	 * Store all statistics contained within a set. The format of each map 
	 * is retained when it is stored.
	 * 
	 * @param dataSet
	 * @throws StatsStorageException
	 */
	void storeStatistics(List<Map<String, Object>> dataSet)
			throws StatsStorageException;

	/**
	 * Store a map after appending a service name to it
	 * 
	 * @param service -- gatherer name
	 * @param rawStats -- map to store
	 * @throws StatsStorageException
	 */
	void storeStatistics(String service, RawStats rawStats)
			throws StatsStorageException;
}
