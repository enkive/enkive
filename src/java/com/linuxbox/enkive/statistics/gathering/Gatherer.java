/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
 * 
 * This file is part of Enkive CE (Community Edition).
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.linuxbox.enkive.statistics.gathering;

import java.util.Date;
import java.util.List;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.services.StatsStorageService;

public interface Gatherer {
	/**
	 * @return returns the gatherer's attributes class
	 */
	public GathererAttributes getAttributes();

	/**
	 * gathers the statistics corresponding to this gatherer
	 * 
	 * @return the gathered statistics
	 * @throws GathererException
	 */
	public RawStats getStatistics() throws GathererException;

	/**
	 * used by some gatherers to get statistics from an interval The rawStats
	 * don't actually have to use the dates provided
	 * 
	 * @param startTimestamp
	 *            - is the idealized lowerbound date that this was run
	 * @param endTimestamp
	 *            - is the idealized upperbound date that this was run
	 * @return the gathered statistics
	 * @throws GathererException
	 */
	public RawStats getStatistics(Date startTimestamp, Date endTimestamp)
			throws GathererException;

	/**
	 * gathers the statistics corresponding to this gatherer and filters them
	 * based on the arrays of keys given as arguments
	 * 
	 * NOTE: if both the string arrays are null throws a gathererException
	 * 
	 * @param intervalStats
	 *            - a string array of interval statistic keys
	 * @param pointStats
	 *            - a string array of point statistic keys
	 * @return a rawStats class containing the statistics specified by the array
	 *         arguments (filters out all unspecified statistics)
	 * @throws GathererException
	 */
	public RawStats getStatistics(List<String> intervalStats,
			List<String> pointStats) throws GathererException;

	/**
	 * sets this gatherer's storage service
	 * 
	 * @param storageService
	 *            - storage service to add to gatherer
	 */
	public void setStorageService(StatsStorageService storageService);

	/**
	 * stores all statistics belonging to this gatherer
	 * 
	 * @throws GathererException
	 */
	public void storeStats() throws GathererException;

	/**
	 * stores all statistics in the supplied argument
	 * 
	 * @throws GathererException
	 */
	public void storeStats(RawStats stats) throws GathererException;
}
