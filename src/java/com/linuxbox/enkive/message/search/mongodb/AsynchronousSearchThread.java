package com.linuxbox.enkive.message.search.mongodb;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Callable;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.authentication.AuthenticationService;
import com.linuxbox.enkive.docsearch.DocSearchQueryService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.permissions.PermissionService;
import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.WorkspaceService;
import com.linuxbox.enkive.workspace.SearchResult.Status;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.mongodb.Mongo;

public class AsynchronousSearchThread extends
		PermissionEnforcingMongoSearchService implements Callable<SearchResult> {

	private final Authentication searchingUserAuth;
	private final String searchResultId;
	private final HashMap<String, String> fields;

	public AsynchronousSearchThread(HashMap<String, String> fields,
			String searchResultId, WorkspaceService workspaceService,
			AuthenticationService authService, AuditService auditService,
			DocSearchQueryService docSearchService,
			PermissionService permService, Mongo m, String dbName,
			String collName) {
		super(permService, m, dbName, collName);
		SecurityContext ctx = SecurityContextHolder.getContext();
		searchingUserAuth = ctx.getAuthentication();
		this.searchResultId = searchResultId;
		this.fields = fields;
		setWorkspaceService(workspaceService);
		setAuthenticationService(authService);
		setAuditService(auditService);
		setDocSearchService(docSearchService);
	}

	@Override
	public SearchResult call() {

		SearchResult searchResult = null;
		try {
			SecurityContext ctx = new SecurityContextImpl();
			ctx.setAuthentication(searchingUserAuth);
			SecurityContextHolder.setContext(ctx);

			searchResult = workspaceService.getSearchResult(searchResultId);
			try {
				markSearchResultRunning(searchResult);

				Set<String> messageIds = searchImpl(fields);
				searchResult.setMessageIds(messageIds);
				searchResult.setStatus(Status.COMPLETE);
				workspaceService.saveSearchResult(searchResult);

			} catch (MessageSearchException e) {
				searchResult.setStatus(Status.UNKNOWN);
				workspaceService.saveSearchResult(searchResult);
				logger.error("Could not complete message search", e);
			}
		} catch (WorkspaceException e) {
			logger.error("Could not complete message search", e);
		} finally {
			SecurityContextHolder.clearContext();
		}

		return searchResult;
	}

	private void markSearchResultRunning(SearchResult searchResult)
			throws MessageSearchException {
		try {
			searchResult.setStatus(Status.RUNNING);
			searchResult.setTimestamp(new Date());
			workspaceService.saveSearchResult(searchResult);
		} catch (WorkspaceException e) {
			throw new MessageSearchException("Could not mark search "
					+ searchResult.getId() + " as running", e);
		}
	}

}
