/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
package com.linuxbox.enkive.statistics.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.linuxbox.enkive.statistics.services.retrieval.StatsFilter;
import com.linuxbox.enkive.statistics.services.retrieval.StatsQuery;
import com.linuxbox.enkive.statistics.services.retrieval.StatsRetrievalException;
import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;

public interface StatsRetrievalService {
	/**
	 * Does a query equivalent to find() on the statistics service back end.
	 * 
	 * @param statName
	 *            A dot-separated path to the statistic desired starting with
	 *            the service name (e.g., "MessageStorageService.count",
	 *            "AuditService.count.max")
	 * @param startingTimestamp
	 * @param endingTimestamp
	 * @return a set of results. Each result could be a simple object (e.g.,
	 *         String, Integer) or something more complex (List, Map).
	 * @throws StatsStorageException
	 */
	public Set<Map<String, Object>> queryStatistics()
			throws StatsRetrievalException;

	/**
	 * Does a query on the backend based on a query Object and a date range
	 * 
	 * @param stats
	 *            - a map formatted as the following: {GathererName:{key1:val1,
	 *            key2:val2}, ...} each key must conform to the correct dot
	 *            notation (e.g. MessageCollection.indexes._id.count)
	 * @param startingTimestamp
	 *            -the starting date for the query
	 * @param endingTimestamp
	 *            -the ending date for the query
	 * @return a set of object cooresponding to each of the object found by the
	 *         query
	 * @throws StatsRetrievalException
	 */
	public Set<Map<String, Object>> queryStatistics(StatsQuery query)
			throws StatsRetrievalException;

	/**
	 * Does a query on the backend that only returns specific keys as specified
	 * by the filterMap's entries NOTE: serviceName and timestamp will always be
	 * returned regardless of filterMap
	 * 
	 * @param queryMap
	 *            - the query object used to generate the query Must be of
	 *            format: {gathererName:{key:val, key:val...}...}
	 * @param filterMap
	 *            - the filter used to only return specific keys from all
	 *            queryed objects must be of format: {GathererName:{key:1,
	 *            key:1...}...}
	 * @return a set of objects only containing the
	 * @throws StatsRetrievalException
	 */
	public List<Map<String, Object>> queryStatistics(
			Map<String, Map<String, Object>> queryMap,
			Map<String, Map<String, Object>> filterMap)
			throws StatsRetrievalException;

	// TODO document
	public List<Map<String, Object>> queryStatistics(List<StatsQuery> query,
			List<StatsFilter> filter) throws StatsRetrievalException;

	/**
	 * Removes every object cooresponding to a given objectID
	 * 
	 * @param deletionSet
	 *            - a set of objectIds
	 * @throws StatsRetrievalException
	 */
	public void remove(Set<Object> deletionSet) throws StatsRetrievalException;

	// TODO
	public Set<Map<String, Object>> queryStatistics(StatsQuery query,
			StatsFilter filter) throws StatsRetrievalException;

	/**
	 * Does a query on the backend without formatting the map at all (e.g. just
	 * like the shell)
	 * 
	 * @param query
	 *            a map that is the exact representation of a dbObject to query
	 *            the DB with
	 * @return set representing the query
	 */
	// public Set<Map<String, Object>> directQuery(Map<String, Object> query);
}
