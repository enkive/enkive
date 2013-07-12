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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.annotation.Async;

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
			final Set<String> resultMessageIDs = searchImpl(fields);

			// complete the search result data
			result.setMessageIds(resultMessageIDs);
			query.setTimestamp(new Date());
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

		TreeSet<String> sortedUUIDs = new TreeSet<String>(
				result.getMessageIds());
		fields.put(INITIAL_MESSAGE_UUID_PARAMETER, sortedUUIDs.last());

		LOGGER.trace("AbstractMessageSearchService.updateSearch function looking for messages w/ following criteria: "
				+ fields.toString());

		// do the search
		final Set<String> resultMessageIDs = searchImpl(fields);

		// Add into the previous results
		resultMessageIDs.addAll(sortedUUIDs);

		// complete the search result data
		result.setMessageIds(resultMessageIDs);
		query.setTimestamp(new Date());
		query.setStatus(Status.COMPLETE);
	}

	@Override
	@Async
	public Future<SearchQuery> searchAsync(final Map<String, String> fields)
			throws MessageSearchException {
		FutureTask<SearchQuery> searchFuture = new FutureTask<SearchQuery>(
				new Callable<SearchQuery>() {
					public SearchQuery call() {
						SearchQuery query = null;
						try {
							query = search(fields);
						} catch (MessageSearchException e) {
							if (LOGGER.isWarnEnabled())
								LOGGER.warn("Error Searching for message", e);
						}
						return query;
					}
				});
		searchFuture.run();
		return searchFuture;
	}

	protected abstract Set<String> searchImpl(Map<String, String> fields)
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
