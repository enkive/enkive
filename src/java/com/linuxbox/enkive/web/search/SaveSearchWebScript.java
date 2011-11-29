/*
 *  Copyright 2010 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.enkive.web.search;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.linuxbox.enkive.exception.EnkiveServletException;
import com.linuxbox.enkive.web.EnkiveServlet;
import com.linuxbox.enkive.web.WebScriptUtils;
import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceService;

public class SaveSearchWebScript extends EnkiveServlet {
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

		String searchId = WebScriptUtils.cleanGetParameter(req, "searchid");
		String nameOfSavedSearch = WebScriptUtils
				.cleanGetParameter(req, "name");
		try {
			SearchResult result = workspaceService.getSearchResult(searchId);
			result.setSaved(true);
			workspaceService.saveSearchResult(result);

			LOGGER.debug("saved search at id " + searchId + " with name \""
					+ nameOfSavedSearch + "\"");
		} catch (WorkspaceException e) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, res);
			throw new EnkiveServletException("Could not mark search at UUID "
					+ searchId + "as saved", e);
		}
	}
}
