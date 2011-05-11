package com.linuxbox.enkive.docsearch.indri;

import java.util.Collection;
import java.util.LinkedList;
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

	static final String SYSTEM_PATH_SEPARATOR = System.getProperty(
			"path.separator", ":");

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.docsearch.indri");

	private static final String NAME_FIELD = "docno";
	private static final String[] METADATA_FIELDS = { NAME_FIELD };

	private static final QueryResultToDocNameConverter QUERY_RESULT_CONVERTER;

	private Collection<String> indexPaths;
	private Collection<String> indexServers;

	private QueryEnvironment queryEnvironment;

	static {
		QUERY_RESULT_CONVERTER = new QueryResultToDocNameConverter();
	}

	/**
	 * Default constructor. User is expected to call setIndexPaths and/or
	 * setIndexServers thereafter, followed by startup.
	 */
	public IndriDocSearchQueryService() {
		// empty
	}

	/**
	 * Convenience constructor when there is only a single index path.
	 * 
	 * @param indexPath
	 */
	public IndriDocSearchQueryService(String indexPath) {
		this.indexPaths = new LinkedList<String>();
		this.indexPaths.add(indexPath);
	}

	public IndriDocSearchQueryService(Collection<String> indexPaths,
			Collection<String> indexServers) {
		this.indexPaths = indexPaths;
		this.indexServers = indexServers;
	}

	private void initializeIndexPaths() {
		if (indexPaths == null) {
			return;
		}

		for (String path : indexPaths) {
			try {
				queryEnvironment.addIndex(path);
			} catch (Exception e) {
				LOGGER.error("could not add index path \"" + path + "\"", e);
			}
		}
	}

	private void initializeIndexServers() {
		if (indexServers == null) {
			return;
		}

		for (String server : indexServers) {
			try {
				queryEnvironment.addServer(server);
			} catch (Exception e) {
				LOGGER.error("could not add index server \"" + server + "\"", e);
			}
		}
	}

	@Override
	public void startup() throws DocSearchException {
		try {
			queryEnvironment = new QueryEnvironment();
			initializeIndexPaths();
			initializeIndexServers();
		} catch (Exception e1) {
			throw new DocSearchException(
					"could not create an INDRI query environment", e1);
		}
	}

	@Override
	public void shutdown() throws DocSearchException {
		if (queryEnvironment != null) {
			try {
				queryEnvironment.close();
			} catch (Exception e) {
				throw new DocSearchException(
						"unable to close down query environment", e);
			}
		}
	}

	@Override
	public List<String> search(String query, int maxResults)
			throws DocSearchException {
		try {
			final ScoredExtentResult[] results = queryEnvironment.runQuery(
					query, maxResults);
			String[] resultDocNumbers = queryEnvironment.documentMetadata(
					results, NAME_FIELD);
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

	public Collection<String> getIndexPaths() {
		return indexPaths;
	}

	public void setIndexPaths(Collection<String> indexPaths) {
		this.indexPaths = indexPaths;
	}

	/**
	 * Sets the set of index paths that will be queried. Only useful if called
	 * before startup.
	 * 
	 * @param indexPaths
	 *            a colon-separated list (or semicolon-separated list if on
	 *            Windows) of paths; uses the System's "path.separator" property
	 */
	public void setIndexPaths(String indexPaths) {
		String[] indexPathArray = indexPaths.split(SYSTEM_PATH_SEPARATOR);
		this.indexPaths = CollectionUtils.listFromArray(indexPathArray);
	}

	public Collection<String> getIndexServers() {
		return indexServers;
	}

	public void setIndexServers(Collection<String> indexServers) {
		this.indexServers = indexServers;
	}

	/**
	 * Sets the set of index servers that will be queried. Only useful if called
	 * before startup.
	 * 
	 * @param indexServers
	 *            a comma-separated list of servers (either domain name, IP
	 *            address, or anything else that the platform will accept).
	 */
	public void setIndexServers(String indexServers) {
		String[] indexServerArray = indexServers.split(",");
		this.indexServers = CollectionUtils.listFromArray(indexServerArray);
	}
}
