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

import java.util.Collection;
import java.util.Date;

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
	private Collection<String> messageIds;
	private Status status;

	protected SearchResult() {
		this.timestamp = new Date();
		this.status = Status.RUNNING;
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

	public Collection<String> getMessageIds() {
		return messageIds;
	}

	public void setMessageIds(Collection<String> messageIds) {
		this.messageIds = messageIds;
	}
}
