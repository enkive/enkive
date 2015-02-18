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
package com.linuxbox.enkive.web;

import static com.linuxbox.enkive.web.WebConstants.DETAILED_LOCAL_DATE_FORMAT;
import static com.linuxbox.enkive.web.WebConstants.RESULTS_TAG;
import static com.linuxbox.enkive.web.WebPageInfo.PAGE_POSITION_PARAMETER;
import static com.linuxbox.enkive.web.WebPageInfo.PAGE_SIZE_PARAMETER;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.audit.AuditEntry;
import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.AuditServiceDescriptions;
import com.linuxbox.enkive.audit.AuditServiceException;

public class AuditLogServlet extends EnkiveServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3968709794392548799L;

	private AuditService auditService;

	protected static final String AUDIT_ENTRIES_KEY = "audit_entries";
	protected static final String IDENTIFIER_KEY = "id";
	protected static final String TIMESTAMP_KEY = "timestamp";
	protected static final String EVENT_CODE_KEY = "event_code";
	protected static final String USER_NAME_KEY = "user_name";
	protected static final String DESCRIPTION_KEY = "description";
	protected static final String DATE_NUMBER_KEY = "date_number";
	protected static final String AUDIT_TRAIL_SIZE_KEY = "audit_trail_size";

	public AuditLogServlet() {
		super();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		auditService = getAuditService();
		JSONObject jsonResponse = new JSONObject();

		WebPageInfo pageInfo = new WebPageInfo(cleanGetParameter(req,
				PAGE_POSITION_PARAMETER), cleanGetParameter(req,
				PAGE_SIZE_PARAMETER));

		try {

			try {
				JSONObject resultsObject = new JSONObject();
				jsonResponse.put(RESULTS_TAG, resultsObject);

				JSONArray resultsArray = new JSONArray();
				resultsObject.put(AUDIT_ENTRIES_KEY, resultsArray);

				long auditEntryCount = auditService.getAuditEntryCount();

				resultsObject.put(AUDIT_TRAIL_SIZE_KEY, auditEntryCount);

				List<AuditEntry> results = auditService.getMostRecentByPage(
						pageInfo.getPageSize(), pageInfo.getPagePos() - 1);
				for (AuditEntry entry : results) {
					resultsArray.put(createAuditEntryJSONObject(entry));
				}
				String resultsString;
				if (results.size() > 0) {
					final String firstId = results.get(0).getIdentifier();
					final String lastId = results.get(results.size() - 1)
							.getIdentifier();
					resultsString = firstId + "-" + lastId;
				} else {
					resultsString = "no entries retrieved";
				}
				pageInfo.setItemTotal(auditEntryCount);
				jsonResponse.put(WebPageInfo.PAGING_LABEL,
						pageInfo.getPageJSON());
				auditService.addEvent(AuditService.AUDIT_LOG_QUERY,
						getPermissionService().getCurrentUsername(),
						resultsString);
			} catch (AuditServiceException e) {
				LOGGER.error("error creating audit entry ", e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"error creating audit entry " + "; see server logs");

				// if we get an exception, to maintain security do not return
				// any results
				jsonResponse.put(RESULTS_TAG, (JSONObject) null);
			}

		} catch (JSONException e) {
			LOGGER.error("JSONException", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"error transcribing audit trail entries to JSON; see server logs");
		} finally {
			// set Response
			String jsonResultString = jsonResponse.toString();
			try {
				resp.getWriter().write(jsonResultString);
			} catch (IOException e) {
				LOGGER.error("Could not write JSON response");
			}

		}

	}

	private JSONObject createAuditEntryJSONObject(AuditEntry entry)
			throws JSONException {

		JSONObject entryObject = new JSONObject();

		entryObject.put(IDENTIFIER_KEY, entry.getIdentifier());
		entryObject.put(TIMESTAMP_KEY,
				DETAILED_LOCAL_DATE_FORMAT.format(entry.getTimestamp()));
		entryObject.put(EVENT_CODE_KEY, String.valueOf(entry.getEventCode()));
		entryObject.put(USER_NAME_KEY, entry.getUserName());
		entryObject.put(DATE_NUMBER_KEY,
				String.valueOf(entry.getTimestamp().getTime()));
		entryObject.put(DESCRIPTION_KEY, buildDescription(entry));

		return entryObject;

	}

	private String buildDescription(AuditEntry entry) {
		String description = AuditServiceDescriptions.getEventDescription(entry
				.getEventCode());
		if (description == null) {
			description = "unknown type";
		}

		String descriptionEntry = AuditServiceDescriptions
				.getDescriptionContent(entry.getEventCode());
		if (entry.getDescription() != null && descriptionEntry != null) {
			description += "; " + descriptionEntry + " "
					+ entry.getDescription();
		}

		return description;
	}

	private static String cleanGetParameter(HttpServletRequest request,
			String parameterName) {
		String parameterValue = request.getParameter(parameterName);

		if (parameterValue == null || parameterValue.equalsIgnoreCase("null")) {
			return null;
		} else {
			return parameterValue;
		}
	}

}
