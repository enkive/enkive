package com.linuxbox.enkive.message.search.mongodb;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.linuxbox.enkive.authentication.AuthenticationException;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.permissions.PermissionService;
import com.linuxbox.enkive.workspace.SearchQuery;
import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.SearchResult.Status;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.util.threadpool.CancellableProcessExecutor;
import com.mongodb.Mongo;

public class TaskPoolPermEnforcingMongoSearchService extends
		PermissionEnforcingMongoSearchService {

	CancellableProcessExecutor searchExecutor;

	public TaskPoolPermEnforcingMongoSearchService(
			PermissionService permService, Mongo m, String dbName,
			String collName, int corePoolSize, int maxPoolSize,
			int keepAliveTime) {
		super(permService, m, dbName, collName);
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
		searchExecutor = new CancellableProcessExecutor(corePoolSize,
				maxPoolSize, keepAliveTime, TimeUnit.SECONDS, queue);
	}

	@Override
	public Future<SearchResult> searchAsync(HashMap<String, String> fields)
			throws MessageSearchException {

		String searchResultId = createSearchResult(fields);

		Callable<SearchResult> searchCall = new AsynchronousSearchThread(
				fields, searchResultId, workspaceService,
				authenticationService, auditService, docSearchService,
				permService, m, messageDb.getName(), messageColl.getName());

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
			SearchResult searchResult = workspaceService
					.getSearchResult(searchResultId);

			searchResult.setStatus(Status.CANCEL_REQUESTED);
			workspaceService.saveSearchResult(searchResult);

			searchCancelled = searchExecutor.cancelSearch(searchResultId);

			searchResult.setStatus(Status.CANCEL_REQUESTED);
			workspaceService.saveSearchResult(searchResult);
		} catch (WorkspaceException e) {
			throw new MessageSearchException("Could not mark search "
					+ searchResultId + " as canceled", e);
		}
		return searchCancelled;

	}

	private String createSearchResult(HashMap<String, String> fields)
			throws MessageSearchException {
		try {
			Workspace workspace = workspaceService
					.getActiveWorkspace(authenticationService.getUserName());
			SearchQuery query = new SearchQuery();
			query.setCriteria(fields);

			query.setId(workspaceService.saveSearchQuery(query));

			SearchResult result = new SearchResult();
			result.setSearchQueryId(query.getId());
			result.setExecutedBy(authenticationService.getUserName());
			result.setStatus(Status.QUEUED);
			String resultId = workspaceService.saveSearchResult(result);
			result.setId(resultId);
			workspace.addSearchResult(resultId);
			workspaceService.saveWorkspace(workspace);
			return resultId;
		} catch (WorkspaceException e) {
			throw new MessageSearchException("Could not save search query", e);
		} catch (AuthenticationException e) {
			throw new MessageSearchException(
					"Could not get authenticated user for search", e);
		}
	}

}
