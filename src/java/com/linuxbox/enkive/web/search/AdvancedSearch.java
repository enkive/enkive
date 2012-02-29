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

import static com.linuxbox.enkive.search.Constants.CONTENT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.MESSAGE_ID_PARAMETER;
import static com.linuxbox.enkive.search.Constants.RECIPIENT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.SENDER_PARAMETER;
import static com.linuxbox.enkive.search.Constants.SUBJECT_PARAMETER;
import static com.linuxbox.enkive.web.WebConstants.COMPLETE_STATUS_VALUE;
import static com.linuxbox.enkive.web.WebConstants.DATA_TAG;
import static com.linuxbox.enkive.web.WebConstants.QUERY_TAG;
import static com.linuxbox.enkive.web.WebConstants.RESULTS_TAG;
import static com.linuxbox.enkive.web.WebConstants.RUNNING_STATUS_VALUE;
import static com.linuxbox.enkive.web.WebConstants.SEARCH_ID_TAG;
import static com.linuxbox.enkive.web.WebConstants.STATUS_ID_TAG;
import static com.linuxbox.enkive.web.WebPageInfo.PAGING_LABEL;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.authentication.AuthenticationService;
import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.exception.EnkiveServletException;
import com.linuxbox.enkive.message.MessageSummary;
import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.retriever.MessageRetrieverService;
import com.linuxbox.enkive.web.WebConstants;
import com.linuxbox.enkive.web.WebPageInfo;
import com.linuxbox.enkive.web.WebScriptUtils;
import com.linuxbox.enkive.workspace.SearchResult;

public class AdvancedSearch extends AbstractSearchWebScript {

	/**
	 * 
	 */
	private static final long serialVersionUID = -782959587635819894L;
	protected MessageRetrieverService archiveService;
	protected MessageSearchService searchService;
	protected AuditService auditService;
	protected AuthenticationService authenticationService;

	protected int searchTimeoutSeconds = 15;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.searchService = getMessageSearchService();
		this.auditService = getAuditService();
		this.authenticationService = getAuthenticationService();
		this.archiveService = getMessageRetrieverService();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		JSONObject jsonData = new JSONObject();
		JSONObject jsonResult = new JSONObject();

		try {
			// because of the JavaScript SpringSurf bridge we need to look out
			// for parameters that contain the string "null" rather than the
			// null value, so we call cleanGetParameter
			String sender = WebScriptUtils.cleanGetParameter(req,
					SENDER_PARAMETER);
			String recipient = WebScriptUtils.cleanGetParameter(req,
					RECIPIENT_PARAMETER);
			String dateEarliestString = WebScriptUtils.cleanGetParameter(req,
					DATE_EARLIEST_PARAMETER);
			String dateLatestString = WebScriptUtils.cleanGetParameter(req,
					DATE_LATEST_PARAMETER);
			String subject = WebScriptUtils.cleanGetParameter(req,
					SUBJECT_PARAMETER);
			String messageId = WebScriptUtils.cleanGetParameter(req,
					MESSAGE_ID_PARAMETER);
			String contentCriteriaString = WebScriptUtils.cleanGetParameter(
					req, CONTENT_PARAMETER);

			// note that the content portion of the query will have been cleaned
			// up by the parsing of the contentCriteriaString
			jsonData.put(
					QUERY_TAG,
					searchQueryToJson(sender, recipient, dateEarliestString,
							dateLatestString, subject, messageId,
							contentCriteriaString));

			HashMap<String, String> searchFields = new HashMap<String, String>();
			searchFields.put(SENDER_PARAMETER, sender);
			searchFields.put(RECIPIENT_PARAMETER, recipient);
			searchFields.put(DATE_EARLIEST_PARAMETER, dateEarliestString);
			searchFields.put(DATE_LATEST_PARAMETER, dateLatestString);
			searchFields.put(SUBJECT_PARAMETER, subject);
			searchFields.put(MESSAGE_ID_PARAMETER, messageId);
			searchFields.put(CONTENT_PARAMETER, contentCriteriaString);

			SearchResult result = null;

			try {
				Future<SearchResult> resultFuture = searchService
						.searchAsync(searchFields);
				result = resultFuture.get(searchTimeoutSeconds,
						TimeUnit.SECONDS);

			} catch (Exception e) {
				// catch various kinds of exceptions, including cancellations
				result = null;
			}

			if (result != null) {
				jsonData.put(SEARCH_ID_TAG, result.getId());
				WebPageInfo pageInfo = new WebPageInfo();
				if (LOGGER.isInfoEnabled())
					LOGGER.info("search results are complete");

				final List<MessageSummary> messageSummaries = archiveService
						.retrieveSummary(result.getMessageIds());

				pageInfo.setItemTotal(messageSummaries.size());
				@SuppressWarnings("unchecked")
				final JSONArray jsonMessageSummary = SearchResultsBuilder
						.getMessageListJSON((List<MessageSummary>) pageInfo
								.getSubList(messageSummaries));
				jsonData.put(RESULTS_TAG, jsonMessageSummary);

				jsonData.put(STATUS_ID_TAG, COMPLETE_STATUS_VALUE);
				jsonData.put(WebConstants.ITEM_TOTAL_TAG,
						pageInfo.getItemTotal());

				jsonResult.put(PAGING_LABEL, pageInfo.getPageJSON());
			} else {
				if (LOGGER.isInfoEnabled())
					LOGGER.info("search results are not ready yet");
				jsonData.put(STATUS_ID_TAG, RUNNING_STATUS_VALUE);
			}
			jsonResult.put(DATA_TAG, jsonData);
		} catch (JSONException e) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("JSONException", e);
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					res);
			throw new EnkiveServletException("Unable to serialize JSON");
		} catch (CannotRetrieveException e) {
			if (LOGGER.isFatalEnabled())
				LOGGER.fatal(
						"could not retrieve message summaries from archive", e);
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					res);
			throw new EnkiveServletException("Unable to access repository");
		} finally {
			setResponse(res, jsonResult);
		}
	}
}
