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
package com.linuxbox.enkive.search;

import static com.linuxbox.enkive.GeneralConstants.STANDARD_TIME_ZONE;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Constants {
	public static final String SENDER_PARAMETER = "sender";
	public static final String RECIPIENT_PARAMETER = "recipient";
	public static final String DATE_EARLIEST_PARAMETER = "dateEarliest";
	public static final String DATE_LATEST_PARAMETER = "dateLatest";
	public static final String SUBJECT_PARAMETER = "subject";
	public static final String MESSAGE_ID_PARAMETER = "messageId";
	public static final String CONTENT_PARAMETER = "content";

	public static final DateFormat NUMERIC_SEARCH_FORMAT;

	static {
		NUMERIC_SEARCH_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
		NUMERIC_SEARCH_FORMAT.setTimeZone(STANDARD_TIME_ZONE);
	}

}
