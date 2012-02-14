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

import static com.linuxbox.enkive.web.WebConstants.SIMPLE_LOCAL_DATE_FORMAT;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.message.MessageSummary;
import com.linuxbox.enkive.web.WebConstants;

public class SearchResultsBuilder {
	protected static final Log logger = LogFactory
			.getLog("com.linuxbox.enkive.webscripts.search");

	public static String getJSONString(MessageSummary messageSummary)
			throws JSONException {
		JSONObject jObject = getMessageJSON(messageSummary);
		return jObject.toString();
	}

	public static String getJSONString(
			Collection<MessageSummary> messageSummaries) throws JSONException {
		JSONArray jArray = getMessageListJSON(messageSummaries);
		return jArray.toString();
	}

	public static JSONArray getMessageListJSON(
			Collection<MessageSummary> messageSummaries) throws JSONException {
		JSONArray emailRecordArray = new JSONArray();
		for (MessageSummary messageSummary : messageSummaries) {
			emailRecordArray.put(getMessageJSON(messageSummary));
		}
		return emailRecordArray;
	}

	public static JSONObject getMessageJSON(MessageSummary messageSummary)
			throws JSONException {
		JSONObject jsonMessageSummary = new JSONObject();

		jsonMessageSummary.put(WebConstants.MESSAGE_ID_TAG,
				messageSummary.getId());
		jsonMessageSummary.put(WebConstants.MESSAGE_SUBJECT_TAG,
				messageSummary.getSubject());
		jsonMessageSummary.put(WebConstants.MESSAGE_SENDER_TAG,
				messageSummary.getFrom());
		jsonMessageSummary.put(WebConstants.MESSAGE_RECIPIENTS_TAG,
				messageSummary.getTo());
		jsonMessageSummary.put(WebConstants.MESSAGE_DATE_TAG,
				SIMPLE_LOCAL_DATE_FORMAT.format(messageSummary.getDate()));
		jsonMessageSummary.put(WebConstants.MESSAGE_DATENUMBER_TAG,
				messageSummary.getDate().getTime());

		return jsonMessageSummary;
	}
}
