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
package com.linuxbox.enkive.web.search;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.GeneralConstants;
import com.linuxbox.enkive.authentication.AuthenticationException;
import com.linuxbox.enkive.exception.EnkiveServletException;
import com.linuxbox.enkive.web.EnkiveServlet;
import com.linuxbox.enkive.web.WebConstants;
import com.linuxbox.enkive.web.WebPageInfo;
import com.linuxbox.enkive.web.WebScriptUtils;
import com.linuxbox.enkive.workspace.SearchQuery;
import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceService;

public abstract class AbstractSearchListServlet extends EnkiveServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5385773633334840889L;

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.webscripts");

	WorkspaceService workspaceService;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		workspaceService = getWorkspaceService();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Retrieving search list");
		try {
			WebPageInfo pageInfo = new WebPageInfo(
					WebScriptUtils.cleanGetParameter(req,
							WebPageInfo.PAGE_POSITION_PARAMETER),
					WebScriptUtils.cleanGetParameter(req,
							WebPageInfo.PAGE_SIZE_PARAMETER));

			JSONObject jObject = new JSONObject();
			jObject.put(WebConstants.DATA_TAG, getWorkspaceSearches(pageInfo));
			jObject.put(WebPageInfo.PAGING_LABEL, pageInfo.getPageJSON());
			String jsonString = jObject.toString();
			resp.getWriter().write(jsonString);
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Returned search list");
		} catch (JSONException e) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Error retrieving search list", e);
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					resp);
			throw new EnkiveServletException("Unable to serialize JSON");
		}
	}

	protected JSONArray getWorkspaceSearches(WebPageInfo pageInfo) {
		JSONArray searches = new JSONArray();

		try {
			Workspace workspace = workspaceService
					.getActiveWorkspace(getAuthenticationService()
							.getUserName());
			List<SearchResult> searchResults = getSearches(workspace);

			pageInfo.setItemTotal(searchResults.size());
			@SuppressWarnings("unchecked")
			List<SearchResult> searchesSublist = (List<SearchResult>) pageInfo
					.getSubList(searchResults);

			for (SearchResult searchResult : searchesSublist) {
				try {

					SearchQuery searchQuery = workspaceService
							.getSearchQuery(searchResult.getSearchQueryId());
					JSONObject search = new JSONObject();
					search.put(WebConstants.SEARCH_ID_TAG, searchResult.getId());

					search.put(WebConstants.SEARCH_NAME_TAG,
							searchQuery.getName());
					search.put(WebConstants.STATUS_ID_TAG,
							searchResult.getStatus());
					search.put(WebConstants.SEARCH_IS_SAVED,
							searchResult.isSaved());
					search.put(WebConstants.SEARCH_DATE_TAG,
							GeneralConstants.NUMERIC_FORMAT_W_MILLIS
									.format(searchResult.getTimestamp()));

					JSONArray criteriaArray = new JSONArray();
					for (Entry<String, String> criterion : searchQuery
							.getCriteria().entrySet()) {
						JSONObject criterionObject = new JSONObject();

						criterionObject.put(WebConstants.SEARCH_PARAMETER_TAG,
								criterion.getKey());
						criterionObject.put(WebConstants.SEARCH_VALUE_TAG,
								criterion.getValue());
						criteriaArray.put(criterionObject);
					}

					search.put("criteria", criteriaArray);
					searches.put(search);

				} catch (JSONException e) {
					if (LOGGER.isWarnEnabled())
						LOGGER.warn("error creating JSON object for search "
								+ searchResult.getId());
				}
			}
		} catch (WorkspaceException e) {
			if (LOGGER.isWarnEnabled())
				LOGGER.warn("error accessing workspace for retrieval of searches");
			if (LOGGER.isDebugEnabled())
				LOGGER.debug(
						"error accessing workspace for retrieval of searches",
						e);
		} catch (AuthenticationException e) {
			if (LOGGER.isWarnEnabled())
				LOGGER.warn("error accessing workspace for retrieval of searches");
			if (LOGGER.isDebugEnabled())
				LOGGER.debug(
						"error accessing workspace for retrieval of searches",
						e);
		}

		return searches;
	}

	abstract List<SearchResult> getSearches(Workspace workspace)
			throws WorkspaceException;

}
