package com.linuxbox.enkive.statistics.gathering;

import java.util.Map;

import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;

public interface GathererInterface {
	/**
	 * @return returns this gatherer's attributes class
	 */
	public GathererAttributes getAttributes();

	/**
	 * gathers all the statistics cooresponding to this gatherer
	 * @return the gathered statistics
	 * @throws GathererException
	 */
	public Map<String, Object> getStatistics() throws GathererException;

	/**
	 * gathers the statistics cooresponding to this gatherer but filtered by the
	 * string array 'keys'--only those keys in the array are returned
	 * 
	 * NOTE: if the string array is null all keys are returned
	 * 
	 * @param keys - a string array of keys
	 * @return a filtered version of the statistics
	 * @throws GathererException
	 */
	public Map<String, Object> getStatistics(String[] keys)
			throws GathererException;

	/**
	 * @param storageService StatsStorageService to set gatherer to use
	 */
	public void setStorageService(StatsStorageService storageService);

	/**
	 * if the result of the getStatistics() is not null store it in mongo
	 * @throws StatsStorageException
	 */
	public void storeStats() throws StatsStorageException;
}
