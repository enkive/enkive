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
import com.linuxbox.enkive.web.WebConstants;
import com.linuxbox.enkive.web.WebPageInfo;
import com.linuxbox.enkive.web.WebScriptUtils;
import com.linuxbox.enkive.workspace.SearchQuery;
import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;

/**
 * This webscript is run when a user wants to see the results of a prior search,
 * either recent or saved
 */
public class ViewSavedResultsWebScript extends AbstractWorkspaceWebscript {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1226107681645083623L;

	protected static final Log logger = LogFactory
			.getLog("com.linuxbox.enkive.webscripts.search.saved");

	MessageRetrieverService archiveService;

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		try {
			String searchId = WebScriptUtils.cleanGetParameter(req, "id");
			
			WebPageInfo pageInfo = new WebPageInfo(
					WebScriptUtils.cleanGetParameter(req,
							PAGE_POSITION_PARAMETER),
					WebScriptUtils.cleanGetParameter(req, PAGE_SIZE_PARAMETER));
			
			logger.info("in execute workspace service is set to "
					+ workspaceService);
			Workspace workspace = workspaceService.getActiveWorkspace();

			JSONObject dataJSON = new JSONObject();
			JSONObject jsonResult = new JSONObject();
			dataJSON.put(SEARCH_ID_TAG, searchId);
			
			/* Query */

			SearchQuery searchQuery = workspaceService.readQuery(workspace,
					searchId);

			JSONObject jsonCriteria = new JSONObject();
			try {
				for (String parameter : searchQuery.getCriteriaParameters()) {
					String value = searchQuery.getCriteriumValue(parameter);
					jsonCriteria.put(parameter, value);
				}
			} catch (JSONException e) {
				logger.error("could not return search criteria for search "
						+ searchId, e);
			}
			dataJSON.put(QUERY_TAG, jsonCriteria);

			/* Message Result List */

			try {
				List<SearchResult> searchResults = workspaceService
						.readResults(searchQuery);

				SearchResult theResult;
				if (searchResults.size() == 0) {
					logger.error("no results for search " + searchId);
					throw new EnkiveServletException(
							"Unable to access results for search " + searchId);
				} else if (searchResults.size() == 1) {
					theResult = searchResults.get(0);
				} else {
					logger
							.warn("search "
									+ searchId
									+ " had "
									+ searchResults.size()
									+ " search results, but was only expecting one for now (until search queries can be re-executed)");
					// returning last result
					theResult = searchResults.get(searchResults.size() - 1);
				}
				List<String> messageIds = new ArrayList<String>(theResult.getMessageIds());
				@SuppressWarnings("unchecked")
				List<MessageSummary> messageSummaries = archiveService
						.retrieveSummary((List<String>) pageInfo.getSubList(messageIds));
				pageInfo.setTotal(messageIds.size());
				dataJSON.put(WebConstants.STATUS_ID_TAG, theResult.getStatus());

				JSONArray jsonMessageSummaryList = SearchResultsBuilder
						.getMessageListJSON((Collection<MessageSummary>)messageSummaries);
				
				dataJSON.put(ITEM_TOTAL_TAG, pageInfo.getItemTotal());
				
				dataJSON.put(RESULTS_TAG, jsonMessageSummaryList);
			} catch (CannotRetrieveException e) {
				logger.error("Could not access query result message list", e);
				// throw new WebScriptException(
				// "Could not access query result message list", e);
			}

			logger.debug("Returning saved search results for search id "
					+ searchId);

			jsonResult.put(DATA_TAG, dataJSON);
			jsonResult.put(PAGING_LABEL, pageInfo.getPageJSON());
			res.getWriter().write(jsonResult.toString());
		} catch (WorkspaceException e) {
			throw new EnkiveServletException(
					"Could not login to repository to retrieve search", e);
		} catch (JSONException e) {
			throw new EnkiveServletException("Unable to serialize JSON", e);
		}
	}
}
