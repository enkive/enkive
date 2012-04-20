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

import static com.linuxbox.enkive.web.WebConstants.DATA_TAG;
import static com.linuxbox.enkive.web.WebConstants.ITEM_TOTAL_TAG;
import static com.linuxbox.enkive.web.WebConstants.QUERY_TAG;
import static com.linuxbox.enkive.web.WebConstants.RESULTS_TAG;
import static com.linuxbox.enkive.web.WebConstants.SEARCH_ID_TAG;
import static com.linuxbox.enkive.web.WebPageInfo.PAGE_POSITION_PARAMETER;
import static com.linuxbox.enkive.web.WebPageInfo.PAGE_SIZE_PARAMETER;
import static com.linuxbox.enkive.web.WebPageInfo.PAGING_LABEL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.exception.EnkiveServletException;
import com.linuxbox.enkive.message.MessageSummary;
import com.linuxbox.enkive.retriever.MessageRetrieverService;
import com.linuxbox.enkive.web.EnkiveServlet;
import com.linuxbox.enkive.web.WebConstants;
import com.linuxbox.enkive.web.WebPageInfo;
import com.linuxbox.enkive.web.WebScriptUtils;
import com.linuxbox.enkive.workspace.SearchQuery;
import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceService;

/**
 * This webscript is run when a user wants to see the results of a prior search,
 * either recent or saved
 */
public class ViewSavedResultsServlet extends EnkiveServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1226107681645083623L;

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.webscripts.search.saved");

	protected MessageRetrieverService archiveService;
	protected WorkspaceService workspaceService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.workspaceService = getWorkspaceService();
		this.archiveService = getMessageRetrieverService();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		try {
			String searchId = WebScriptUtils.cleanGetParameter(req, "id");

			WebPageInfo pageInfo = new WebPageInfo(
					WebScriptUtils.cleanGetParameter(req,
							PAGE_POSITION_PARAMETER),
					WebScriptUtils.cleanGetParameter(req, PAGE_SIZE_PARAMETER));

			JSONObject dataJSON = new JSONObject();
			JSONObject jsonResult = new JSONObject();
			dataJSON.put(SEARCH_ID_TAG, searchId);
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Loading " + searchId);

			SearchResult searchResult = workspaceService
					.getSearchResult(searchId);
			SearchQuery searchQuery = workspaceService
					.getSearchQuery(searchResult.getSearchQueryId());

			JSONObject jsonCriteria = new JSONObject();
			/* Query */
			try {
				for (String parameter : searchQuery.getCriteriaParameters()) {
					String value = searchQuery.getCriteriumValue(parameter);
					jsonCriteria.put(parameter, value);
				}
			} catch (JSONException e) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error("could not return search criteria for search "
							+ searchId, e);
			}
			dataJSON.put(QUERY_TAG, jsonCriteria);

			/* Message Result List */

			try {

				List<String> messageIds = new ArrayList<String>(
						searchResult.getMessageIds());
				@SuppressWarnings("unchecked")
				List<MessageSummary> messageSummaries = archiveService
						.retrieveSummary((List<String>) pageInfo
								.getSubList(messageIds));
				pageInfo.setItemTotal(messageIds.size());
				dataJSON.put(WebConstants.STATUS_ID_TAG,
						searchResult.getStatus());

				JSONArray jsonMessageSummaryList = SearchResultsBuilder
						.getMessageListJSON((Collection<MessageSummary>) messageSummaries);

				dataJSON.put(ITEM_TOTAL_TAG, pageInfo.getItemTotal());

				dataJSON.put(RESULTS_TAG, jsonMessageSummaryList);
			} catch (CannotRetrieveException e) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error("Could not access result message list", e);
				// throw new WebScriptException(
				// "Could not access query result message list", e);
			}
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Returning saved search results for search id "
						+ searchId);

			jsonResult.put(DATA_TAG, dataJSON);
			jsonResult.put(PAGING_LABEL, pageInfo.getPageJSON());
			res.getWriter().write(jsonResult.toString());
		} catch (WorkspaceException e) {
			respondError(HttpServletResponse.SC_UNAUTHORIZED, null, res);
			throw new EnkiveServletException(
					"Could not login to repository to retrieve search", e);
		} catch (JSONException e) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					res);
			throw new EnkiveServletException("Unable to serialize JSON", e);
		} finally {

		}
	}
}
