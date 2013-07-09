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

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.authentication.AuthenticationException;
import com.linuxbox.enkive.authentication.AuthenticationService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceService;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;
import com.linuxbox.enkive.workspace.searchResult.SearchResult.Status;
import com.linuxbox.enkive.workspace.searchResult.SearchResultBuilder;
import com.linuxbox.util.threadpool.CancellableProcessExecutor;

public class TaskPoolAsyncMessageSearchService implements MessageSearchService {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.message.search");

	CancellableProcessExecutor searchExecutor;
	MessageSearchService messageSearchService;
	WorkspaceService workspaceService;
	AuthenticationService authenticationService;
	SearchResultBuilder searchResultBuilder;
	SearchQueryBuilder searchQueryBuilder;

	public TaskPoolAsyncMessageSearchService(int corePoolSize, int maxPoolSize,
			int keepAliveTime) {
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
		searchExecutor = new CancellableProcessExecutor(corePoolSize,
				maxPoolSize, keepAliveTime, TimeUnit.SECONDS, queue);
	}

	@Override
	public Future<SearchResult> searchAsync(Map<String, String> fields)
			throws MessageSearchException {
		String searchResultId = createSearchResult(fields);

		Callable<SearchResult> searchCall = new AsynchronousSearchThread(
				fields, searchResultId, messageSearchService,
				searchResultBuilder);

		try {
			@SuppressWarnings("unchecked")
			Future<SearchResult> searchFuture = (Future<SearchResult>) searchExecutor
					.submit(searchResultId, searchCall);
			return searchFuture;
		} catch (Exception e) {
			LOGGER.error("Error with asynchronous search", e);
		}
		return null;
	}

	public boolean cancelAsyncSearch(String searchResultId)
			throws MessageSearchException {

		boolean searchCancelled = false;

		try {
			SearchResult searchResult = searchResultBuilder
					.getSearchResult(searchResultId);

			searchResult.setStatus(Status.CANCEL_REQUESTED);
			searchResult.saveSearchResult();

			searchCancelled = searchExecutor.cancelSearch(searchResultId);

			if (searchCancelled)
				searchResult.setStatus(Status.CANCELED);
			else
				searchResult.setStatus(Status.ERROR);
			searchResult.saveSearchResult();
		} catch (WorkspaceException e) {
			throw new MessageSearchException("Could not mark search "
					+ searchResultId + " as canceled", e);
		}
		return searchCancelled;

	}

	private String createSearchResult(Map<String, String> fields)
			throws MessageSearchException {
		try {
			Workspace workspace = workspaceService
					.getActiveWorkspace(authenticationService.getUserName());

			SearchQuery query = searchQueryBuilder.getSearchQuery();
			query.setCriteria(fields);
			query.saveSearchQuery();

			SearchResult result = searchResultBuilder.getSearchResult();
			result.setSearchQueryId(query.getId());
			result.setExecutedBy(authenticationService.getUserName());
			result.setStatus(Status.QUEUED);
			result.saveSearchResult();

			query.setResultId(result.getId());
			query.saveSearchQuery();

			workspace.setLastQueryUUID(query.getId());
			workspace.addSearchResult(result.getId());
			workspace.saveWorkspace();
			return result.getId();
		} catch (WorkspaceException e) {
			throw new MessageSearchException("Could not save search query", e);
		} catch (AuthenticationException e) {
			throw new MessageSearchException(
					"Could not get authenticated user for search", e);
		}
	}

	@Override
	public SearchResult search(Map<String, String> fields)
			throws MessageSearchException {
		return messageSearchService.search(fields);
	}

	public MessageSearchService getMessageSearchService() {
		return messageSearchService;
	}

	public void setMessageSearchService(
			MessageSearchService messageSearchService) {
		this.messageSearchService = messageSearchService;
	}

	public WorkspaceService getWorkspaceService() {
		return workspaceService;
	}

	public void setWorkspaceService(WorkspaceService workspaceService) {
		this.workspaceService = workspaceService;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public SearchResultBuilder getSearchResultBuilder() {
		return searchResultBuilder;
	}

	public void setSearchResultBuilder(SearchResultBuilder searchResultBuilder) {
		this.searchResultBuilder = searchResultBuilder;
	}

	public SearchQueryBuilder getSearchQueryBuilder() {
		return searchQueryBuilder;
	}

	public void setSearchQueryBuilder(SearchQueryBuilder searchQueryBuilder) {
		this.searchQueryBuilder = searchQueryBuilder;
	}

	public void shutdown() {
		this.searchExecutor.shutdown();
	}
}
