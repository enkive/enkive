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
package com.linuxbox.enkive.web;

import javax.servlet.http.HttpServletRequest;

public class WebScriptUtils {

	/**
	 * Due to the JavaScript bridge, we get the string "null" for a nonexistent
	 * parameter rather than an actual null value. This addresses that issue.
	 * 
	 * @param request
	 * @param parameterName
	 * @return
	 */
	public static String cleanGetParameter(HttpServletRequest request,
			String parameterName) {
		String parameterValue = request.getParameter(parameterName);

		if (parameterValue == null || parameterValue.equalsIgnoreCase("null")) {
			return null;
		} else {
			return parameterValue;
		}
	}

	/**
	 * Decodes an integer parameter. Accepts decimal, octal, and hexadecimal
	 * values.
	 * 
	 * @param request
	 *            the webscript request object
	 * @param parameterName
	 *            the webscript parameter name
	 * @param defaultValue
	 *            the default value to use if no such parameter can be found or
	 *            if its value is illegal
	 * @return
	 */
	public static int decodeIntegerParameter(HttpServletRequest request,
			String parameterName, int defaultValue) {
		String string = request.getParameter(parameterName);
		if (string == null) {
			return defaultValue;
		}
		try {
			int value = Integer.decode(string);
			return value;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

}
