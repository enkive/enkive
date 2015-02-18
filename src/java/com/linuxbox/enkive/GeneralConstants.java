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
package com.linuxbox.enkive;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class GeneralConstants {
	public static final TimeZone STANDARD_TIME_ZONE = TimeZone.getDefault();

	public static final DecimalFormat TWO_DIGIT_INTEGER = new DecimalFormat(
			"00");

	public static final DateFormat NUMERIC_FORMAT_W_MILLIS;

	public static final String DEFAULT_LOG_DIRECTORY = "data/logs";

	static {
		NUMERIC_FORMAT_W_MILLIS = new SimpleDateFormat(
				"yyyy-MM-dd_HH-mm-ss-SSS");
		NUMERIC_FORMAT_W_MILLIS.setTimeZone(STANDARD_TIME_ZONE);
	}
}
