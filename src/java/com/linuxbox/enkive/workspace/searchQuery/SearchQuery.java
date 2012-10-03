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
package com.linuxbox.enkive.workspace.searchQuery;

import static com.linuxbox.enkive.search.Constants.CONTENT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.MESSAGE_ID_PARAMETER;
import static com.linuxbox.enkive.search.Constants.RECIPIENT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.SENDER_PARAMETER;
import static com.linuxbox.enkive.search.Constants.SUBJECT_PARAMETER;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.linuxbox.enkive.message.search.MessageSearchSummary;
import com.linuxbox.enkive.workspace.WorkspaceException;

/**
 * Represents a query consisting of a set of search criteria.
 * 
 * @author eric
 * 
 */
public abstract class SearchQuery {
	protected String id;
	protected String name;
	protected Map<String, String> criteria;

	public SearchQuery() {
		criteria = new HashMap<String, String>();
		name = new Date().toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addAllSearchCriteria(MessageSearchSummary searchSummary) {
		addAllSearchCriteria(searchSummary.sender, searchSummary.recipient,
				searchSummary.dateEarliest, searchSummary.dateLatest,
				searchSummary.subject, searchSummary.messageId,
				searchSummary.content);
	}

	public void addAllSearchCriteria(String sender, String recipient,
			Date dateEarliest, Date dateLatest, String subject,
			String messageId, String content) {
		if (sender != null && !sender.isEmpty())
			addCriteria(SENDER_PARAMETER, sender);
		if (recipient != null && !recipient.isEmpty())
			addCriteria(RECIPIENT_PARAMETER, recipient);
		if (dateEarliest != null)
			addCriteria(DATE_EARLIEST_PARAMETER, dateEarliest.toString());
		if (dateLatest != null)
			addCriteria(DATE_LATEST_PARAMETER, dateLatest.toString());
		if (subject != null && !subject.isEmpty())
			addCriteria(SUBJECT_PARAMETER, subject);
		if (messageId != null && !messageId.isEmpty())
			addCriteria(MESSAGE_ID_PARAMETER, messageId);
		if (content != null && !content.isEmpty())
			addCriteria(CONTENT_PARAMETER, content);
	}

	public Map<String, String> getCriteria() {
		return criteria;
	}

	public void setCriteria(Map<String, String> criteria) {
		this.criteria = criteria;
	}

	public void addCriteria(String parameter, String value) {
		criteria.put(parameter, value);
	}

	public String getCriteriumValue(String parameter) {
		return criteria.get(parameter);
	}

	public Collection<String> getCriteriaParameters() {
		return criteria.keySet();
	}

	public abstract void saveSearchQuery() throws WorkspaceException;

	public abstract void deleteSearchQuery() throws WorkspaceException;
}
