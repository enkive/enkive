/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
 ******************************************************************************/
package com.linuxbox.enkive.message.search;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Async;

import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.authentication.AuthenticationException;
import com.linuxbox.enkive.authentication.AuthenticationService;
import com.linuxbox.enkive.docsearch.DocSearchQueryService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.SearchQuery;
import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.SearchResult.Status;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceService;

public abstract class AbstractMessageSearchService implements
		MessageSearchService {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.message.search");

	protected AuthenticationService authenticationService;
	protected WorkspaceService workspaceService;
	protected AuditService auditService;
	protected DocSearchQueryService docSearchService;

	@Override
	public SearchResult search(HashMap<String, String> fields)
			throws MessageSearchException {

		SearchQuery query = new SearchQuery();
		query.setCriteria(fields);

		try {
			Workspace workspace = workspaceService
					.getActiveWorkspace(authenticationService.getUserName());

			query.setId(workspaceService.saveSearchQuery(query));

			SearchResult result = new SearchResult();
			result.setSearchQueryId(query.getId());

			result.setMessageIds(searchImpl(fields));
			result.setTimestamp(new Date());
			result.setExecutedBy(authenticationService.getUserName());
			result.setStatus(Status.COMPLETE);
			String resultId = workspaceService.saveSearchResult(result);
			result.setId(resultId);
			workspace.addSearchResult(resultId);
			workspaceService.saveWorkspace(workspace);
			return result;
		} catch (WorkspaceException e) {
			throw new MessageSearchException("Could not save search query", e);
		} catch (AuthenticationException e) {
			throw new MessageSearchException(
					"Could not get authenticated user for search", e);
		}
	}

	@Override
	@Async
	public Future<SearchResult> searchAsync(final HashMap<String, String> fields)
			throws MessageSearchException {
		FutureTask<SearchResult> searchFuture = new FutureTask<SearchResult>(
				new Callable<SearchResult>() {
					public SearchResult call() {
						SearchResult result = null;
						try {
							result = search(fields);
						} catch (MessageSearchException e) {
							LOGGER.warn("Error Searching for message", e);
						}
						return result;
					}
				});
		searchFuture.run();
		return searchFuture;
	}

	protected abstract Set<String> searchImpl(HashMap<String, String> fields)
			throws MessageSearchException;

	public DocSearchQueryService getDocSearchService() {
		return docSearchService;
	}

	public void setDocSearchService(DocSearchQueryService docSearchService) {
		this.docSearchService = docSearchService;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public WorkspaceService getWorkspaceService() {
		return workspaceService;
	}

	public void setWorkspaceService(WorkspaceService workspaceService) {
		this.workspaceService = workspaceService;
	}

	public AuditService getAuditService() {
		return auditService;
	}

	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

}
