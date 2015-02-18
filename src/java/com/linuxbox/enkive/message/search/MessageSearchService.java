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
package com.linuxbox.enkive.message.search;

import java.util.Map;
import java.util.concurrent.Future;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;

/**
 * Interface for a service to perform searches for messages.  Provides for
 * Synchronous and Asynchronous searches, as well as for updating an existing
 * search.
 * @author dang
 *
 */
public interface MessageSearchService {

	/**
	 * Perform a search for a user and return a populated SearchQuery object.
	 * This search function stores results in a workspace and stores audit log
	 * events.
	 * 
	 * @param fields
	 * @return query
	 * @throws MessageSearchErception
	 */
	SearchQuery search(Map<String, String> fields)
			throws MessageSearchException;

	/**
	 * Update the existing results of a previous query for any changes in the database.
	 *
	 * @param query	Previous query to update
	 * @throws MessageSearchErception
	 */
	void updateSearch(SearchQuery query)
			throws MessageSearchException;

	/**
	 * Perform an asynchronous search and return a Future<SearchResult> object.
	 * 
	 * @param fields
	 * @return Future that will get query when done
	 * @throws MessageSearchErception
	 */
	Future<SearchQuery> searchAsync(Map<String, String> fields)
			throws MessageSearchException;

	/**
	 * Perform an asynchronous searchUpdate and return a Future<SearchResult> object.
	 *
	 * @param query
	 * @return Future that will get query when done
	 * @throws MessageSearchErception
	 */
	Future<SearchQuery> updateSearchAsync(SearchQuery query)
			throws MessageSearchException;

	/**
	 * Cancel an asynchronous search or updateSearch based on the search identifier
	 * 
	 * @param searchId
	 * @return true if cancelled, false otherwise
	 * @throws MessageSearchException
	 */
	boolean cancelAsyncSearch(String searchId) throws MessageSearchException;

}
