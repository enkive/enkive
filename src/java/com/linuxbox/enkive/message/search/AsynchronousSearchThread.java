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
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.SearchResult.Status;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceService;

public class AsynchronousSearchThread implements Callable<SearchResult> {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.message.search");

	private final Authentication searchingUserAuth;
	private final String searchResultId;
	private final HashMap<String, String> fields;
	private final WorkspaceService workspaceService;
	private final MessageSearchService messageSearchService;

	public AsynchronousSearchThread(HashMap<String, String> fields,
			String searchResultId, MessageSearchService messageSearchService,
			WorkspaceService workspaceService) {
		SecurityContext ctx = SecurityContextHolder.getContext();
		searchingUserAuth = ctx.getAuthentication();
		this.searchResultId = searchResultId;
		this.fields = fields;
		this.workspaceService = workspaceService;
		this.messageSearchService = messageSearchService;
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
				SearchResult tmpSearchResult = messageSearchService
						.search(fields);
				searchResult.setMessageIds(tmpSearchResult.getMessageIds());
				searchResult.setTimestamp(tmpSearchResult.getTimestamp());
				searchResult.setExecutedBy(tmpSearchResult.getExecutedBy());
				searchResult.setStatus(Status.COMPLETE);
				workspaceService.saveSearchResult(searchResult);

			} catch (MessageSearchException e) {
				searchResult.setStatus(Status.UNKNOWN);
				workspaceService.saveSearchResult(searchResult);
				if (LOGGER.isErrorEnabled())
					LOGGER.error("Could not complete message search", e);
			}
		} catch (WorkspaceException e) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Could not complete message search", e);
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
