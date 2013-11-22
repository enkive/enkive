/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public interface WebConstants {
	// TODO: using a customized format in order to include day of week with
	// abbreviated day of week and month names, which is likely useful. But it
	// is not locale-aware. If we use a locale aware version, we don't get
	// abbreviated day of week and month names. Perhaps this can be fixed with
	// some code that consults the locale and constructs an appropriate String
	// for SimpleDate.
	DateFormat DETAILED_LOCAL_DATE_FORMAT = new SimpleDateFormat(
			"EEE, MMM dd, yyyy hh:mm:ss aa zzz");

	// DateFormat DETAILED_LOCAL_DATE_FORMAT =
	// SimpleDateFormat.getDateTimeInstance(
	// DateFormat.MEDIUM, DateFormat.FULL);

	DateFormat SIMPLE_LOCAL_DATE_FORMAT = DateFormat.getDateTimeInstance(
			DateFormat.MEDIUM, DateFormat.MEDIUM);

	public static final String DATA_TAG = "data";
	public static final String ERRORS_TAG = "errors";
	public static final String VERSION_LOCAL = "versionLocal";
	public static final String VERSION_REMOTE = "versionRemote";
	public static final String VERSION_UPGRADE = "versionUpgrade";

	// Search results related constants
	public static final String RESULTS_TAG = "results";
	public static final String QUERY_TAG = "query";
	public static final String SEARCH_ID_TAG = "searchId";
	public static final String SEARCH_NAME_TAG = "searchName";
	public static final String SEARCH_DATE_TAG = "searchDate";
	public static final String SEARCH_PARAMETER_TAG = "parameter";
	public static final String SEARCH_VALUE_TAG = "value";
	public static final String SEARCH_IS_SAVED = "searchIsSaved";
	public static final String SEARCH_IS_IMAP = "searchIsIMAP";
	public static final String ITEM_TOTAL_TAG = "itemTotal";
	public static final String ERROR_MESSAGE_TAG = "errorMessage";

	public static final String MESSAGE_ID_TAG = "messageId";
	public static final String MESSAGE_DATE_TAG = "date";
	public static final String MESSAGE_DATENUMBER_TAG = "datenumber";
	public static final String MESSAGE_SUBJECT_TAG = "subject";
	public static final String MESSAGE_SENDER_TAG = "sender";
	public static final String MESSAGE_RECIPIENTS_TAG = "recipients";

	public static final String RESULT_ID_TAG = "resultId";
	public static final String STATUS_ID_TAG = "status";

	public static final String COMPLETE_STATUS_VALUE = "complete";
	public static final String RUNNING_STATUS_VALUE = "running";
	public static final String ERROR_STATUS_VALUE = "error";
	public static final String STOPPED_STATUS_VALUE = "stopped";

	public static final String USERNAME_TAG = "userName";
	public static final String PERMISSION_ADDRESSES_TAG = "addresses";
	public static final String PERMISSION_ADMIN_TAG = "admin";
	public static final String PERMISSION_CAN_READ_ALL_TAG = "can_read_all";
	public static final String USER_AUTHORITIES = "userAuthorities";

	public static final String SORTBYDATE = "sortByDate";
	public static final String SORTBYSTATUS = "sortByStatus";
	public static final String SORTBYNAME = "sortByName";
	public static final String SORTBYSUBJECT = "sortBySubject";
	public static final String SORTBYSENDER = "sortBySender";
	public static final String SORTBYRECEIVER = "sortByReceiver";

	public static final int SORT_ASC = 1;
	public static final int SORT_DESC = -1;

}
