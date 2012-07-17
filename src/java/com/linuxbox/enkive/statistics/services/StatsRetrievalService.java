package com.linuxbox.enkive.statistics.services;

import java.util.Date;
import java.util.Map;
import java.util.Set;

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
	 * Does a query on a date range to the statistics back-end
	 * @param startingTimestamp -the starting date for the query
	 * @param endingTimestamp -the ending date for the query
	 * @return a set of results. Each result is a map object retrieved from the
	 * db between the two dates specified
	 * @throws StatsRetrievalException
	 */
	public Set<Map<String, Object>> queryStatistics(Date startingTimestamp,
			Date endingTimestamp) throws StatsRetrievalException;

	/**
	 * Does a query on the backend based on gatherer types & keys
	 * @param stats - a map formatted as the following:
	 * {GathererName:{key1:val1, key2:val2}, ...} each key must conform
	 * to the correct dot notation (e.g. MessageCollection.indexes._id.count)
	 * @return a set of results cooresponding to each of the objects found by 
	 * the query
	 * @throws StatsRetrievalException
	 */
	public Set<Map<String, Object>> queryStatistics(
			Map<String, Map<String, Object>> stats)
			throws StatsRetrievalException;

	/**
	 * Does a query on the backend based on a query Object and a date range
	 * @param stats - a map formatted as the following:
	 * {GathererName:{key1:val1, key2:val2}, ...} each key must conform
	 * to the correct dot notation (e.g. MessageCollection.indexes._id.count)
	 * @param startingTimestamp -the starting date for the query
	 * @param endingTimestamp -the ending date for the query
	 * @return a set of object cooresponding to each of the object found by the query
	 * @throws StatsRetrievalException
	 */
	public Set<Map<String, Object>> queryStatistics(
			Map<String, Map<String, Object>> stats, Date startingTimestamp,
			Date endingTimestamp) throws StatsRetrievalException;

	/**
	 * Does a query on the backend that only returns specific keys as specified by the filterMap's
	 * entries, note that they must always be using proper dot notation and each filter key's val
	 * must be 1
	 * NOTE: serviceName and timestamp will always be returned regardless of filterMap
	 * @param queryMap - the query object used to generate the query Must be of format:
	 * {gathererName:{key:val, key:val...}...}
	 * @param filterMap - the filter used to only return specific keys from all queryed objects
	 * must be of format: {GathererName:{key:1, key:1...}...}
	 * @return a set of objects only containing the 
	 * @throws StatsRetrievalException
	 */
	public Set<Map<String, Object>> queryStatistics(
			Map<String, Map<String, Object>> queryMap,
			Map<String, Map<String, Object>> filterMap)
			throws StatsRetrievalException;

	/**
	 * Removes every object cooresponding to a given objectID
	 * @param deletionSet - a set of objectIds
	 * @throws StatsRetrievalException
	 */
	public void remove(Set<Object> deletionSet) throws StatsRetrievalException;

	/**
	 * Equivalent to the coll.find() method
	 * @return every object in the collection
	 */
	public Set<Map<String, Object>> directQuery();
	
	/**
	 * Does a query on the backend without formatting the map at all (e.g. just 
	 * like the shell)
	 * @param query a map that is the exact representation of a dbObject to query
	 * the DB with
	 * @return set representing the query
	 */
	public Set<Map<String, Object>> directQuery(Map<String, Object> query);
}
