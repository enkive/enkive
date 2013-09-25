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
package com.linuxbox.enkive.web.search;

import static com.linuxbox.enkive.search.Constants.SEARCH_ENABLE_PARAMETER;
import static com.linuxbox.enkive.search.Constants.SEARCH_IDS_PARAMETER;
import static com.linuxbox.enkive.search.Constants.SEARCH_NAME_PARAMETER;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.linuxbox.enkive.web.EnkiveServlet;
import com.linuxbox.enkive.web.WebScriptUtils;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceService;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;

public class IMAPSearchWebScript extends EnkiveServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = 8342072157628116473L;

	protected WorkspaceService workspaceService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.workspaceService = getWorkspaceService();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {

		ArrayList<String> failedSavedSearches = new ArrayList<String>();
		String searchIds = WebScriptUtils.cleanGetParameter(req, SEARCH_IDS_PARAMETER);
		String nameOfIMAPSearch = WebScriptUtils.cleanGetParameter(req, SEARCH_NAME_PARAMETER);
		String enable = WebScriptUtils.cleanGetParameter(req, SEARCH_ENABLE_PARAMETER);

		for (String searchId : searchIds.split(",")) {
			if (!searchId.isEmpty()) {
				try {
					SearchQuery query = workspaceService.getSearch(searchId);
					if (enable.toLowerCase().equals("true")) {
						query.setIMAP(true);
					} else {
						query.setIMAP(false);
					}
					if (nameOfIMAPSearch != null) {
						query.setName(URLDecoder.decode(nameOfIMAPSearch, "UTF-8"));
					}
					query.saveSearchQuery();

/*					Workspace workspace = workspaceService
							.getActiveWorkspace(this.getPermissionService()
									.getCurrentUsername());

					SearchFolder searchFolder = workspace.getSearchFolder();
					searchFolder.addSearchResult(result);
					searchFolder.saveSearchFolder();*/

					if (LOGGER.isDebugEnabled())
						LOGGER.debug("IMAP for search at id " + searchId
								+ " with name \"" + nameOfIMAPSearch + "\" marked " + enable);

				} catch (WorkspaceException e) {
					failedSavedSearches.add(searchId);
				}
			}
		}
		if (!failedSavedSearches.isEmpty()) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					res);
			throw new IOException("Could not save searches with UUIDs "
					+ failedSavedSearches.toString());
		}
	}
}
