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
 *******************************************************************************/
package com.linuxbox.enkive.web.search;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.web.EnkiveServlet;
import com.linuxbox.enkive.web.WebScriptUtils;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceService;

public class DeleteSearchWebScript extends EnkiveServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -184507901343376549L;
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.webscripts.search.saved");

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {

		String searchId = "";
		try {
			searchId = WebScriptUtils.cleanGetParameter(req, "searchid");
			WorkspaceService workspaceService = getWorkspaceService();
			Workspace workspace = workspaceService.getActiveWorkspace(req
					.getUserPrincipal().getName());
			workspace.deleteSearchResult(searchId);

			workspaceService.deleteSearchResult(searchId);
			workspaceService.saveWorkspace(workspace);
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("deleted search at id " + searchId);
		} catch (WorkspaceException e) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					res);
			throw new IOException("Could not delete search at UUID " + searchId);
		}
	}
}
