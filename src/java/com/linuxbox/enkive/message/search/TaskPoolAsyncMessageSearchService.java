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
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery.Status;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;
import com.linuxbox.enkive.workspace.searchResult.SearchResultBuilder;
import com.linuxbox.util.threadpool.CancellableProcessExecutor;

/**
 * Implementation of @ref MessageSearchService that wraps another one (currently
 * @ref RetentionPolicyEnforcingMessageSearchService) that runs asynchronous
 * searches in a TaskPool.
 * @author dang
 *
 */
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
	public Future<SearchQuery> searchAsync(Map<String, String> fields)
			throws MessageSearchException {
		SearchQuery query = createSearch(fields);

		Callable<SearchQuery> searchCall = new AsynchronousSearchThread(
				query, false, messageSearchService);

		try {
			@SuppressWarnings("unchecked")
			Future<SearchQuery> searchFuture = (Future<SearchQuery>) searchExecutor
					.submit(query.getId(), searchCall);
			return searchFuture;
		} catch (Exception e) {
			LOGGER.error("Error with asynchronous search", e);
		}
		return null;
	}

	@Override
	public Future<SearchQuery> updateSearchAsync(SearchQuery query)
			throws MessageSearchException {
		Callable<SearchQuery> searchCall = new AsynchronousSearchThread(
				query, true, messageSearchService);

		try {
			@SuppressWarnings("unchecked")
			Future<SearchQuery> searchFuture = (Future<SearchQuery>) searchExecutor
					.submit(query.getId(), searchCall);
			return searchFuture;
		} catch (Exception e) {
			LOGGER.error("Error with asynchronous search", e);
		}
		return null;
	}

	@Override
	public boolean cancelAsyncSearch(String searchId)
			throws MessageSearchException {

		boolean searchCancelled = false;

		try {
			SearchQuery query = searchQueryBuilder.getSearchQuery(searchId);

			query.setStatus(Status.CANCEL_REQUESTED);
			query.saveSearchQuery();

			searchCancelled = searchExecutor.cancelSearch(searchId);

			if (searchCancelled)
				query.setStatus(Status.CANCELED);
			else
				query.setStatus(Status.ERROR);
			query.saveSearchQuery();
		} catch (WorkspaceException e) {
			throw new MessageSearchException("Could not mark search "
					+ searchId + " as canceled", e);
		}
		return searchCancelled;

	}

	private SearchQuery createSearch(Map<String, String> fields)
			throws MessageSearchException {
		try {
			Workspace workspace = workspaceService
					.getActiveWorkspace(authenticationService.getUserName());

			SearchQuery query = searchQueryBuilder.getSearchQuery();
			query.setCriteria(fields);
			query.setStatus(Status.QUEUED);
			query.setExecutedBy(authenticationService.getUserName());
			query.saveSearchQuery();

			workspace.setLastQueryUUID(query.getId());
			workspace.addSearch(query.getId());
			workspace.saveWorkspace();
			return query;
		} catch (WorkspaceException e) {
			throw new MessageSearchException("Could not save search query", e);
		} catch (AuthenticationException e) {
			throw new MessageSearchException(
					"Could not get authenticated user for search", e);
		}
	}

	@Override
	public SearchQuery search(Map<String, String> fields)
			throws MessageSearchException {
		return messageSearchService.search(fields);
	}

	@Override
	public void updateSearch(SearchQuery query)
			throws MessageSearchException {
		messageSearchService.updateSearch(query);
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
