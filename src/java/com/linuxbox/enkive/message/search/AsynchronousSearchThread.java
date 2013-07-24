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

import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery.Status;

/**
 * Callable to run a given search and create the results.  This is to allow
 * asynchronous searching, and is designed to be run from a TaskPool.
 * @author dang
 *
 */
public class AsynchronousSearchThread implements Callable<SearchQuery> {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.message.search");

	private final Authentication searchingUserAuth;
	private final SearchQuery query;
	private final MessageSearchService messageSearchService;
	private final Boolean update;

	public AsynchronousSearchThread(SearchQuery query, Boolean update,
			MessageSearchService messageSearchService) {
		SecurityContext ctx = SecurityContextHolder.getContext();
		searchingUserAuth = ctx.getAuthentication();
		this.query = query;
		this.update = update;
		this.messageSearchService = messageSearchService;
	}

	@Override
	public SearchQuery call() {

		try {
			SecurityContext ctx = new SecurityContextImpl();
			ctx.setAuthentication(searchingUserAuth);
			SecurityContextHolder.setContext(ctx);
			try {
				markSearchRunning(query);
				if (update) {
					messageSearchService.updateSearch(query);
				} else {
					SearchQuery tmpSearchQuery = messageSearchService
							.search(query.getCriteria());
					query.copy(tmpSearchQuery);
				}
				query.setStatus(Status.COMPLETE);
				query.saveSearchQuery();

			} catch (MessageSearchException e) {
				query.setStatus(Status.UNKNOWN);
				query.saveSearchQuery();
				LOGGER.error("Could not complete message search", e);
			}
		} catch (WorkspaceException e) {
			LOGGER.error("Could not complete message search", e);
		} finally {
			SecurityContextHolder.clearContext();
		}

		return query;
	}

	private void markSearchRunning(SearchQuery search)
			throws MessageSearchException {
		try {
			search.setStatus(Status.RUNNING);
			search.setTimestamp(new Date());
			search.saveSearchQuery();
		} catch (WorkspaceException e) {
			throw new MessageSearchException("Could not mark search "
					+ search.getId() + " as running", e);
		}
	}

}
