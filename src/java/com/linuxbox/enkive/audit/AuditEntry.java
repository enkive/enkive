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

package com.linuxbox.enkive.audit;

import java.util.Date;

/**
 * Represents an Audit Entry. Currently its properties can only be set in the
 * constructor. There are no setters. Perhaps this should change.
 * 
 * @author eric
 * 
 */
public class AuditEntry {
	private String identifier;
	private Date timestamp;
	private int eventCode;
	private String userName;
	private String description;

	public AuditEntry(String identifier, Date timestamp, int eventCode,
			String userName, String description) {
		this.identifier = identifier;
		this.timestamp = timestamp;
		this.eventCode = eventCode;
		this.userName = userName;
		this.description = description;
	}

	public String getIdentifier() {
		return identifier;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public int getEventCode() {
		return eventCode;
	}

	public String getUserName() {
		return userName;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AuditEntry").append(" [identifier=").append(identifier)
				.append(", timestamp=").append(timestamp)
				.append(", eventCode=").append(eventCode).append(", userName=")
				.append(userName).append(", description=").append(description)
				.append("]");
		return builder.toString();
	}
}
