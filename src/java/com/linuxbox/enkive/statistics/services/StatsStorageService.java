package com.linuxbox.enkive.statistics.services;

import java.util.Map;
import java.util.Set;

import com.linuxbox.enkive.statistics.storage.StatsStorageException;

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
	void storeStatistics(String service, Map<String, Object> data) throws StatsStorageException;
	
	void storeStatistics(Set<Map<String, Object>> dataSet) throws StatsStorageException;
}
