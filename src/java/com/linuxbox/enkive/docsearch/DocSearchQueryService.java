package com.linuxbox.enkive.docsearch;

import java.util.List;

import com.linuxbox.enkive.docsearch.exception.DocSearchException;

public interface DocSearchQueryService {
	/**
	 * Shut down the service and release any resources used by the service.
	 * 
	 * @throws DocSearchException
	 */
	void startup() throws DocSearchException;

	/**
	 * Shut down the service and release any resources used by the service.
	 * 
	 * @throws DocSearchException
	 */
	void shutdown() throws DocSearchException;

	// QUERYING

	/*
	 * NOTE: We will likely want to abstract the 'search' methods further after
	 * additional thought, so as not to have to send in a query via the specific
	 * implementation.
	 */

	/**
	 * Perform a search and return a list of document identifiers that match.
	 * 
	 * @param query
	 * 
	 * @return
	 * @throws DocSearchException
	 */
	List<String> search(String query) throws DocSearchException;

	/**
	 * Perform a search and return a list of document identifiers that match.
	 * 
	 * @param query
	 * @param rawSearch
	 *            if true submit query string to engine w/o any
	 *            pre-processing/parsing/composing
	 * 
	 * @return
	 * @throws DocSearchException
	 */
	List<String> search(String query, boolean rawSearch)
			throws DocSearchException;

	/**
	 * Perform a search and return a list of document identifiers that match.
	 * 
	 * @param query
	 * @param rawSearch
	 *            if true submit query string to engine w/o any
	 *            pre-processing/parsing/composing
	 * 
	 * @return
	 * @throws DocSearchException
	 */
	List<String> search(String query, int maxResults) throws DocSearchException;

	/**
	 * Perform a search and return a list of document identifiers that match, at
	 * most maxResults of them.
	 * 
	 * @param query
	 * @param maxResults
	 * @return
	 */

	List<String> search(String query, int maxResults, boolean rawSearch)
			throws DocSearchException;
}
