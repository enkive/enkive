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

package com.linuxbox.enkive.audit;

import static com.linuxbox.enkive.audit.AuditService.AUDIT_LOG_QUERY;
import static com.linuxbox.enkive.audit.AuditService.MESSAGE_ARCHIVED;
import static com.linuxbox.enkive.audit.AuditService.MESSAGE_ARCHIVE_FAILURE;
import static com.linuxbox.enkive.audit.AuditService.MESSAGE_EMERGENCY_SAVED;
import static com.linuxbox.enkive.audit.AuditService.MESSAGE_PURGED;
import static com.linuxbox.enkive.audit.AuditService.MESSAGE_RETRIEVED;
import static com.linuxbox.enkive.audit.AuditService.ATTACHMENT_RETRIEVED;
import static com.linuxbox.enkive.audit.AuditService.SEARCH_PERFORMED;
import static com.linuxbox.enkive.audit.AuditService.SYSTEM_SHUTDOWN;
import static com.linuxbox.enkive.audit.AuditService.SYSTEM_STARTUP;
import static com.linuxbox.enkive.audit.AuditService.SYSTEM_UPGRADE;
import static com.linuxbox.enkive.audit.AuditService.USER_LOGS_IN;
import static com.linuxbox.enkive.audit.AuditService.USER_LOGS_OUT;

import java.util.HashMap;
import java.util.Map;

public class AuditServiceDescriptions {
	static class EventDescription {
		public String eventName;
		public String descriptionContent;

		public EventDescription(String eventName, String descriptionContent) {
			this.eventName = eventName;
			this.descriptionContent = descriptionContent;
		}
	}

	protected static Map<Integer, EventDescription> descriptions;

	static {
		descriptions = new HashMap<Integer, EventDescription>();
		descriptions.put(SYSTEM_STARTUP, new EventDescription("system startup",
				null));
		descriptions.put(SYSTEM_SHUTDOWN, new EventDescription(
				"system shutdown", null));
		descriptions.put(SYSTEM_UPGRADE, new EventDescription("system upgrade",
				"identifier"));

		descriptions.put(AUDIT_LOG_QUERY, new EventDescription(
				"audit log query", "entries"));

		descriptions.put(USER_LOGS_IN, new EventDescription("user logs in",
				"entries"));
		descriptions.put(USER_LOGS_OUT, new EventDescription("user logs out",
				"entries"));
		descriptions.put(SEARCH_PERFORMED, new EventDescription(
				"archive search", "search criteria"));

		descriptions.put(MESSAGE_RETRIEVED, new EventDescription(
				"message retrieved", "message id"));
		descriptions.put(ATTACHMENT_RETRIEVED, new EventDescription(
				"attachment retrieved retrieved", "attachment retrieved id"));
		descriptions.put(MESSAGE_ARCHIVED, new EventDescription(
				"message archived", "message id"));
		descriptions.put(MESSAGE_EMERGENCY_SAVED, new EventDescription(
				"message emergency saved", "file name"));
		descriptions.put(MESSAGE_ARCHIVE_FAILURE, new EventDescription(
				"message archive failure", null));
		descriptions.put(MESSAGE_PURGED, new EventDescription("message purged",
				"message id"));
	}

	public static String getEventDescription(int eventNumber) {
		EventDescription description = descriptions.get(eventNumber);
		if (description != null) {
			return description.eventName;
		} else {
			return null;
		}
	}

	public static String getDescriptionContent(int eventNumber) {
		EventDescription description = descriptions.get(eventNumber);
		if (description != null) {
			return description.descriptionContent;
		} else {
			return null;
		}
	}
}
