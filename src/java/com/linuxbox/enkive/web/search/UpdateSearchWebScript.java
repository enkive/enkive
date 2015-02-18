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
package com.linuxbox.enkive.web.search;

import static com.linuxbox.enkive.search.Constants.SEARCH_IDS_PARAMETER;
import static com.linuxbox.enkive.search.Constants.SEARCH_NAME_PARAMETER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.web.EnkiveServlet;
import com.linuxbox.enkive.web.WebScriptUtils;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceService;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;

public class UpdateSearchWebScript extends EnkiveServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = 8342072157628116473L;

	protected WorkspaceService workspaceService;
	protected MessageSearchService searchService;
	protected int searchTimeoutSeconds = 15;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.workspaceService = getWorkspaceService();
		this.searchService = getMessageSearchService();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {

		ArrayList<String> failedUpdates = new ArrayList<String>();
		String searchIds = WebScriptUtils.cleanGetParameter(req, SEARCH_IDS_PARAMETER);
		String nameOfSavedSearch = WebScriptUtils.cleanGetParameter(req, SEARCH_NAME_PARAMETER);

		res.setCharacterEncoding("UTF-8");
		for (String searchId : searchIds.split(",")) {
			if (!searchId.isEmpty()) {
				try {
					SearchQuery query = workspaceService.getSearch(searchId);

					// Async update
					Future<SearchQuery> resultFuture = searchService.updateSearchAsync(query);
					query = resultFuture.get(searchTimeoutSeconds,
							TimeUnit.SECONDS);


					if (LOGGER.isDebugEnabled())
						LOGGER.debug("updated search at id " + searchId
								+ " with name \"" + nameOfSavedSearch + "\"");

				} catch (WorkspaceException e) {
					failedUpdates.add(searchId);
				} catch (MessageSearchException e) {
					failedUpdates.add(searchId);
				} catch (InterruptedException e) {
					failedUpdates.add(searchId);
				} catch (ExecutionException e) {
					failedUpdates.add(searchId);
				} catch (TimeoutException e) {
					failedUpdates.add(searchId);
				}
			}
		}
		if (!failedUpdates.isEmpty()) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					res);
			throw new IOException("Could not update searches with UUIDs "
					+ failedUpdates.toString());
		}
	}
}
