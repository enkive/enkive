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
import com.linuxbox.enkive.workspace.searchResult.SearchResult;
import com.linuxbox.enkive.workspace.searchResult.SearchResult.Status;
import com.linuxbox.enkive.workspace.searchResult.SearchResultBuilder;

public abstract class AbstractMessageSearchService implements
		MessageSearchService {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.message.search");

	protected DocSearchQueryService docSearchService;
	protected SearchResultBuilder searchResultBuilder;

	@Override
	public SearchResult search(Map<String, String> fields)
			throws MessageSearchException {
		try {
			// build an object to hold the search results; search is done
			// further below
			SearchResult result = searchResultBuilder.getSearchResult();
			LOGGER.trace("AbstractMessageSearchService.search function looking for messages w/ following criteria: "
					+ fields.toString());

			// do the search
			final Set<String> resultMessageIDs = searchImpl(fields);

			// complete the search result data
			result.setMessageIds(resultMessageIDs);
			result.setTimestamp(new Date());
			result.setStatus(Status.COMPLETE);

			return result;
		} catch (WorkspaceException e) {
			throw new MessageSearchException(
					"Could not create new search result", e);
		}
	}

	@Override
	public SearchResult updateSearch(SearchQuery query)
			throws MessageSearchException {
		try {
			SearchResult result = searchResultBuilder.getSearchResult(query.getResultId());
			Map<String, String> fields = query.getCriteria();

			TreeSet<String> sortedUUIDs = new TreeSet<String>(result.getMessageIds());

			fields.put(INITIAL_MESSAGE_UUID_PARAMETER, sortedUUIDs.last());
			LOGGER.trace("AbstractMessageSearchService.updateSearch function looking for messages w/ following criteria: "
					+ fields.toString());

			// do the search
			final Set<String> resultMessageIDs = searchImpl(fields);

			// Add into the previous results
			resultMessageIDs.addAll(sortedUUIDs);

			// complete the search result data
			result.setMessageIds(resultMessageIDs);
			result.setTimestamp(new Date());
			result.setStatus(Status.COMPLETE);

			return result;
		} catch (WorkspaceException e) {
			throw new MessageSearchException(
					"Could not create new search result", e);
		}
	}

	@Override
	@Async
	public Future<SearchResult> searchAsync(final Map<String, String> fields)
			throws MessageSearchException {
		FutureTask<SearchResult> searchFuture = new FutureTask<SearchResult>(
				new Callable<SearchResult>() {
					public SearchResult call() {
						SearchResult result = null;
						try {
							result = search(fields);
						} catch (MessageSearchException e) {
							if (LOGGER.isWarnEnabled())
								LOGGER.warn("Error Searching for message", e);
						}
						return result;
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

	public SearchResultBuilder getSearchResultBuilder() {
		return searchResultBuilder;
	}

	@Required
	public void setSearchResultBuilder(SearchResultBuilder searchResultBuilder) {
		this.searchResultBuilder = searchResultBuilder;
	}
}
