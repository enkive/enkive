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

import java.util.Date;
import java.util.List;

public interface AuditService {
	// Here we define numeric codes for the various events. There is a
	// high-level scheme. And if you look at the numbers, there's a low-level
	// scheme as well (XY1=add, XY2=edit, XY3=remove). And currently there are
	// no XY0 codes. When adding event codes please try to make them fit into
	// the current schemes. And if you think there might be a better scheme, let
	// the development team know.

	// SYSTEM EVENTS -- require machine account w/ sufficient privileges
	int SYSTEM_STARTUP = 101;
	int SYSTEM_SHUTDOWN = 102;

	int SYSTEM_UPGRADE = 111;

	// ADMINISTRATIVE EVENTS -- change system settings from withink Enkive;
	// require an Enkive account w/ administrative privileges
	int USER_ADDED = 201;
	int USER_EDITED = 202;
	int USER_REMOVED = 203;

	int POLICY_ADDED = 211;
	int POLICY_EDITED = 212;
	int POLICY_REMOVED = 213;

	int AUDIT_LOG_QUERY = 291;

	// USER EVENTS -- require an Enkive user account with any subset of
	// privileges
	int USER_LOGS_IN = 301;
	int USER_LOGS_OUT = 302;
	int MESSAGE_RETRIEVED = 311;
	int ATTACHMENT_RETRIEVED = 312;
	int SEARCH_PERFORMED = 321;
	int SEARCH_EXPORTED = 322;

	// ARCHIVAL EVENTS -- require no privileges or handled by policy-driven code
	int MESSAGE_ARCHIVED = 401;
	int MESSAGE_EMERGENCY_SAVED = 402;
	int MESSAGE_ARCHIVE_FAILURE = 403;
	int MESSAGE_PURGED = 404;

	// BUILT-IN USERS

	/**
	 * Used for an events that occur outside the system, typically with event
	 * codes in the 100s.
	 */
	String USER_SYSTEM = "system";

	/**
	 * Used for events that occur as part of the various processes at work
	 * within the system (e.g., message comes in and is archived).
	 */
	String USER_PROCESS = "process";

	/**
	 * Adds an event to the audit logs. The timestamp will be determined by the
	 * service.
	 * 
	 * @param eventCode
	 * @param userIdentifier
	 * @param description
	 */
	void addEvent(int eventCode, String userIdentifier, String description)
			throws AuditServiceException;

	void addEvent(int eventCode, String userIdentifier, String description,
			boolean truncateDescription) throws AuditServiceException;

	AuditEntry getEvent(String identifier) throws AuditServiceException;

	/**
	 * Returns a list of all events that match the various criteria. Criteria
	 * that are not searched on should be passed in as null. Note that with
	 * respect to timestamps: startTime <= timestamp < endTime.
	 * 
	 * @param eventCode
	 * @param userIdentifier
	 * @param startTime
	 *            a time that will be BEFORE or EQUAL to any search results
	 * @param endTime
	 *            a time that will be AFTER any search results
	 * @return
	 * @throws AuditServiceException
	 */
	List<AuditEntry> search(Integer eventCode, String userIdentifier,
			Date startTime, Date endTime) throws AuditServiceException;

	/**
	 * Returns perPage entries sorted by date descending (i.e., most recent
	 * first), offsent by page. When page is 0, the first set of perPage entries
	 * is returned.
	 * 
	 * @param perPage
	 * @param pagesToSkip
	 * @return
	 * @throws AuditServiceException
	 */
	List<AuditEntry> getMostRecentByPage(int perPage, int pagesToSkip)
			throws AuditServiceException;

	/**
	 * Returns the number of audit entries in the log that can be accessed.
	 * 
	 * @return
	 * @throws AuditServiceException
	 */
	long getAuditEntryCount() throws AuditServiceException;
}
