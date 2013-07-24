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
package com.linuxbox.enkive.message.search;

import static com.linuxbox.enkive.search.Constants.INITIAL_MESSAGE_UUID_PARAMETER;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import com.linuxbox.enkive.docsearch.DocSearchQueryService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery.Status;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;

/**
 * Common code for implementations of @ref MessageSearchService.  Creates a @ref
 * SearchQuery to represent the search, then calls into the implementation to
 * get the list of messages that match.  These are stored in the results of the query.
 * @author dang
 *
 */
public abstract class AbstractMessageSearchService implements
		MessageSearchService {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.message.search");

	protected DocSearchQueryService docSearchService;
	protected SearchQueryBuilder searchQueryBuilder;

	@Override
	public SearchQuery search(Map<String, String> fields)
			throws MessageSearchException {
		try {
			SearchQuery query = searchQueryBuilder.getSearchQuery();
			query.setCriteria(fields);
			SearchResult result = query.getResult();

			LOGGER.trace("AbstractMessageSearchService.search function looking for messages w/ following criteria: "
					+ fields.toString());

			// do the search
			final TreeMap<String, String> resultMessageIDs = searchImpl(fields);

			// complete the search result data
			result.setMessageIds(new HashSet<String>(resultMessageIDs.values()));
			query.setTimestamp(new Date());
			try {
				query.setLastMonotonic(resultMessageIDs.lastKey());
			} catch (NoSuchElementException e) {
				// Docs say it returns null if empty, but actually throws exception
				query.setLastMonotonic(null);
			}
			query.setStatus(Status.COMPLETE);

			return query;
		} catch (WorkspaceException e) {
			throw new MessageSearchException(
					"Could not create new search result", e);
		}
	}

	@Override
	public void updateSearch(SearchQuery query) throws MessageSearchException {
		SearchResult result = query.getResult();
		Map<String, String> fields = query.getCriteria();

		fields.put(INITIAL_MESSAGE_UUID_PARAMETER, query.getLastMonotonic());

		LOGGER.trace("AbstractMessageSearchService.updateSearch function looking for messages w/ following criteria: "
				+ fields.toString());

		// do the search
		final TreeMap<String, String> resultMessageIDs = searchImpl(fields);

		// Remove the initial message so as not to change the parameter
		fields.remove(INITIAL_MESSAGE_UUID_PARAMETER);

		// complete the search result data
		if (!resultMessageIDs.isEmpty()) {
			result.addMessageIds(resultMessageIDs.values());
			query.setLastMonotonic(resultMessageIDs.lastKey());
			query.setTimestamp(new Date());
			query.setStatus(Status.COMPLETE);
			try {
				query.saveSearchQuery();
			} catch (WorkspaceException e) {
				LOGGER.error("Could not complete message search", e);
			}
		}
	}

	@Override
	public Future<SearchQuery> updateSearchAsync(SearchQuery query)
			throws MessageSearchException {
		throw new MessageSearchException("Unimplemented");
	}

	@Override
	public Future<SearchQuery> searchAsync(final Map<String, String> fields)
			throws MessageSearchException {
		throw new MessageSearchException("Unimplemented");
	}

	@Override
	public boolean cancelAsyncSearch(String searchId)
			throws MessageSearchException {
		throw new MessageSearchException("Unimplemented");
	}

	protected abstract TreeMap<String, String> searchImpl(Map<String, String> fields)
			throws MessageSearchException;

	public DocSearchQueryService getDocSearchService() {
		return docSearchService;
	}

	@Required
	public void setDocSearchService(DocSearchQueryService docSearchService) {
		this.docSearchService = docSearchService;
	}

	public SearchQueryBuilder getSearchQueryBuilder() {
		return searchQueryBuilder;
	}

	@Required
	public void setSearchQueryBuilder(SearchQueryBuilder searchQueryBuilder) {
		this.searchQueryBuilder = searchQueryBuilder;
	}
}
