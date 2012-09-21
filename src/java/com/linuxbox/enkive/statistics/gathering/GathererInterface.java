package com.linuxbox.enkive.statistics.gathering;

import java.util.Date;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.services.StatsStorageService;

public interface GathererInterface {
	/**
	 * @return returns the gatherer's attributes class
	 */
	public GathererAttributes getAttributes();

	/**
	 * gathers the statistics corresponding to this gatherer
	 * @return the gathered statistics
	 * @throws GathererException
	 */
	public RawStats getStatistics() throws GathererException;

	/**
	 * used by some gatherers to get statistics from an interval
	 * The rawStats don't actually have to use the dates provided
	 * @param startTimestamp - is the idealized lowerbound date that this was run
	 * @param endTimestamp - is the idealized upperbound date that this was run
	 * @return the gathered statistics
	 * @throws GathererException
	 */
	public RawStats getStatistics(Date startTimestamp, Date endTimestamp) throws GathererException;
	
	/**
	 * gathers the statistics corresponding to this gatherer and filters them based
	 * on the arrays of keys given as arguments
	 * 
	 * NOTE: if both the string arrays are null throws a gathererException
	 * 
	 * @param intervalStats - a string array of interval statistic keys
	 * @param pointStats - a string array of point statistic keys
	 * @return a rawStats class containing the statistics specified by the array arguments
	 * (filters out all unspecified statistics) 
	 * @throws GathererException
	 */
	public RawStats getStatistics(String[] intervalStats, String[] pointStats)
			throws GathererException;

	/**
	 * sets this gatherer's storage service
	 * @param storageService - storage service to add to gatherer
	 */
	public void setStorageService(StatsStorageService storageService);
	
	/**
	 * stores all statistics belonging to this gatherer
	 * @throws GathererException 
	 */
	public void storeStats() throws GathererException;
	
	/**
	 * stores all statistics in the supplied argument
	 * @throws GathererException 
	 */
	public void storeStats(RawStats stats) throws GathererException;
}
