/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
