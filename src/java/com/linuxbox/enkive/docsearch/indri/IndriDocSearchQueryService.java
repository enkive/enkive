/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
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
 *******************************************************************************/
package com.linuxbox.enkive.docsearch.indri;

import static com.linuxbox.enkive.docsearch.indri.IndriQueryComposer.composeQuery;

import java.util.Collection;
import java.util.List;

import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.QueryRequest;
import lemurproject.indri.QueryResult;
import lemurproject.indri.QueryResults;
import lemurproject.indri.ScoredExtentResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docsearch.AbstractDocSearchQueryService;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.util.CollectionUtils;
import com.linuxbox.util.TypeConverter;

public class IndriDocSearchQueryService extends AbstractDocSearchQueryService {
	/**
	 * Converts a QueryResult into the docno.
	 * 
	 * @author ivancich
	 * 
	 */
	static class QueryResultToDocNameConverter implements
			TypeConverter<QueryResult, String> {
		@Override
		public String convert(QueryResult value) throws Exception {
			return (String) value.metadata.get(NAME_FIELD);
		}
	}

	/**
	 * A filter that removes empty strings (or strings of only whitespace) by
	 * returning null for them. An instance of this class is used in conjunction
	 * w/ CollectionUtils.listFromArray.
	 * 
	 * @author ivancich
	 * 
	 */
	private class RemoveEmptyStringsFilter implements
			CollectionUtils.ItemFilter<String> {
		@Override
		public String doFilter(String input) {
			final String trimmed = input.trim();
			return trimmed.isEmpty() ? null : trimmed;
		}
	}

	private final RemoveEmptyStringsFilter aRemoveEmptyStringsFilter = new RemoveEmptyStringsFilter();

	private static final String SYSTEM_PATH_SEPARATOR = System.getProperty(
			"path.separator", ":");

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.docsearch.indri");

	private static final String NAME_FIELD = "docno";
	private static final String[] METADATA_FIELDS = { NAME_FIELD };

	private static final QueryResultToDocNameConverter QUERY_RESULT_CONVERTER;

	/**
	 * Time between query environment renewals in milliseconds.
	 */
	private static final long DEFAULT_QUERY_ENVIRONMENT_REFRESH_INTERVAL = 15000;

	private QueryEnvironmentManager queryEnvironmentManager;

	static {
		QUERY_RESULT_CONVERTER = new QueryResultToDocNameConverter();
	}

	/**
	 * Default constructor. User is expected to call setIndexPaths and/or
	 * setIndexServers thereafter, followed by startup.
	 */
	public IndriDocSearchQueryService() {
		this.queryEnvironmentManager = new QueryEnvironmentManager(
				DEFAULT_QUERY_ENVIRONMENT_REFRESH_INTERVAL);
	}

	/**
	 * Convenience constructor when there is only a single index path.
	 * 
	 * @param indexPath
	 */
	public IndriDocSearchQueryService(String indexPath) {
		this();
		queryEnvironmentManager.addIndexPath(indexPath);
	}

	public IndriDocSearchQueryService(Collection<String> indexPaths,
			Collection<String> indexServers) {
		this();
		this.queryEnvironmentManager.setIndexPaths(indexPaths);
		this.queryEnvironmentManager.setIndexServers(indexServers);
	}

	@Override
	public void startup() throws DocSearchException {
		// empty
	}

	@Override
	public void shutdown() throws DocSearchException {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("starting shutdown of IndriDocSearchQueryService");
		queryEnvironmentManager.forceQueryEnvironmentRefresh();
		queryEnvironmentManager = null;
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("finished shutdown of IndriDocSearchQueryService");
	}

	@Override
	public List<String> search(String rawQuery, int maxResults,
			boolean rawSearch) throws DocSearchException {
		try {
			String query = rawQuery;

			if (!rawSearch) {
				query = composeQuery(rawQuery).toString();
				if (LOGGER.isTraceEnabled())
					LOGGER.trace("query \"" + rawQuery
							+ "\" became Indri query \"" + query + "\"");
			} else {
				if (LOGGER.isTraceEnabled())
					LOGGER.trace("using raw query \"" + query + "\"");
			}

			final ScoredExtentResult[] results;
			String[] resultDocNumbers;
			final QueryEnvironment queryEnv = queryEnvironmentManager
					.getQueryEnvironment();

			results = queryEnv.runQuery(query, maxResults);
			resultDocNumbers = queryEnv.documentMetadata(results, NAME_FIELD);
			return CollectionUtils.listFromArray(resultDocNumbers);
		} catch (Exception e) {
			throw new DocSearchException("could not perform INDRI query", e);
		}
	}

	/**
	 * This is an alternate implementation of search. It fails when the document
	 * is not stored in the INDRI database.
	 * 
	 * @param query
	 * @param maxResults
	 * @return
	 * @throws DocSearchException
	 */
	@SuppressWarnings("unused")
	private List<String> searchAlt(String query, int maxResults)
			throws DocSearchException {
		try {
			QueryRequest request = new QueryRequest();
			request.query = query;
			request.startNum = 0;
			request.resultsRequested = maxResults;
			request.metadata = METADATA_FIELDS;

			final QueryEnvironment queryEnvironment = queryEnvironmentManager
					.getQueryEnvironment();

			// NB: this call will result in an exception if INDRI does not store
			// a compressed version of the documents
			QueryResults queryResults = queryEnvironment.runQuery(request);

			return CollectionUtils.listFromConvertedArray(queryResults.results,
					QUERY_RESULT_CONVERTER);
		} catch (ClassCastException e) {
			throw new DocSearchException(
					"could not retrieve document identifer from INDRI query");
		} catch (Exception e) {
			throw new DocSearchException("could not perform INDRI query", e);
		}
	}

	// METHODS THAT DELEGATE TO THE QUERYENVIRONMENTMANAGER

	public void refreshQueryEnvironment() {
		queryEnvironmentManager.forceQueryEnvironmentRefresh();
	}

	public Collection<String> getIndexPaths() {
		return queryEnvironmentManager.getIndexPaths();
	}

	public void setIndexPaths(Collection<String> indexPaths) {
		queryEnvironmentManager.setIndexPaths(indexPaths);
	}

	/**
	 * Sets the set of index paths that will be queried. Only useful if called
	 * before startup.
	 * 
	 * @param indexPaths
	 *            a colon-separated list (or semicolon-separated list if on
	 *            Windows) of paths; uses the System's "path.separator" property
	 */
	public void setIndexPathsString(String indexPaths) {
		String[] indexPathArray = indexPaths.split(SYSTEM_PATH_SEPARATOR);
		trimStringArray(indexPathArray);
		queryEnvironmentManager.setIndexPaths(CollectionUtils.listFromArray(
				indexPathArray, aRemoveEmptyStringsFilter));
	}

	public Collection<String> getIndexServers() {
		return queryEnvironmentManager.getIndexServers();
	}

	public void setIndexServers(Collection<String> indexServers) {
		queryEnvironmentManager.setIndexServers(indexServers);
	}

	/**
	 * Sets the set of index servers that will be queried. Only useful if called
	 * before startup.
	 * 
	 * @param indexServers
	 *            a comma-separated list of servers (either domain name, IP
	 *            address, or anything else that the platform will accept).
	 */
	public void setIndexServersString(String indexServers) {
		String[] indexServerArray = indexServers.split(",");
		trimStringArray(indexServerArray);
		queryEnvironmentManager.setIndexServers(CollectionUtils.listFromArray(
				indexServerArray, aRemoveEmptyStringsFilter));
	}

	/**
	 * Number of seconds between QueryEnvironment refreshes.
	 * 
	 * @return
	 */
	public int getQueryEnvironmentRefreshInterval() {
		return (int) (queryEnvironmentManager
				.getQueryEnvironmentRefreshInterval() / 1000);
	}

	/**
	 * Number of seconds that a QueryEnvironment can be used before it is
	 * refreshed.
	 * 
	 * @param queryEnvironmentRefreshInterval
	 */
	public void setQueryEnvironmentRefreshInterval(int refreshIntervalSeconds) {
		queryEnvironmentManager
				.setQueryEnvironmentRefreshInterval(refreshIntervalSeconds * 1000);
	}

	// PRIVATE SUPPORT METHODS

	/**
	 * In case the administrator also included spaces around or between items,
	 * get rid of them.
	 * 
	 * @param stringArray
	 */
	private void trimStringArray(String[] stringArray) {
		for (int i = 0; i < stringArray.length; i++) {
			stringArray[i] = stringArray[i].trim();
		}
	}
}
