/*
 *  Copyright 2011 The Linux Box Corporation.
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

package com.linuxbox.enkive.workspace;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class SearchResult {

	public enum Status {
		RUNNING, // could also mean it's been queued but not started running
		
		COMPLETE,
		
		CANCEL_REQUESTED,
		
		CANCELED,
		
		ERROR,
		
		UNKNOWN; // when the status was read from DB, did not understand
	}

	private String id;
	private Date timestamp;
	protected String executedBy;
	private Set<String> messageIds;
	private Status status;
	protected String searchQueryId;

	public SearchResult() {
		this.timestamp = new Date();
		this.status = Status.RUNNING;
		messageIds = new HashSet<String>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Set<String> getMessageIds() {
		return messageIds;
	}

	public void setMessageIds(Set<String> messageIds) {
		this.messageIds = messageIds;
	}

	public String getSearchQueryId() {
		return searchQueryId;
	}

	public void setSearchQueryId(String searchQueryId) {
		this.searchQueryId = searchQueryId;
	}

	public String getExecutedBy() {
		return executedBy;
	}

	public void setExecutedBy(String executedBy) {
		this.executedBy = executedBy;
	}
}
