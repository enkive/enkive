package com.linuxbox.enkive.statistics.gathering;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.services.StatsStorageService;

public interface GathererInterface {
	/**
	 * @return returns this gatherer's attributes class
	 */
	public GathererAttributes getAttributes();

	/**
	 * gathers all the statistics corresponding to this gatherer
	 * @return the gathered statistics
	 * @throws GathererException
	 */
	public RawStats getStatistics() throws GathererException;

	/**
	 * gathers the statistics corresponding to this gatherer but filtered by the
	 * string array 'keys'--only those keys in the array are returned
	 * 
	 * NOTE: if the string array is null all keys are returned
	 * 
	 * @param keys - a string array of keys
	 * @return a filtered version of the statistics
	 * @throws GathererException
	 */
	public RawStats getStatistics(String[] keys)
			throws GathererException;

	/**
	 * @param storageService StatsStorageService to set gatherer to use
	 */
	public void setStorageService(StatsStorageService storageService);

	/**
	 * if the result of the getStatistics() is not null store it
	 * @throws GathererException 
	 */
	public void storeStats() throws GathererException;
}
