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
package com.linuxbox.enkive.message;

public class Utility {
	public static String stripBracketsFromFromAddress(String fromAddress) {
		if (fromAddress == null || fromAddress.length() <= 0) {
			return "";
		}

		boolean partial = false;

		int startIndex = 0;
		if (fromAddress.charAt(startIndex) == '[') {
			++startIndex;
			partial = true;
		}

		int endIndex = fromAddress.length() - 1;
		if (fromAddress.charAt(endIndex) == ']') {
			--endIndex;
			partial = true;
		}

		if (partial) {
			return fromAddress.substring(startIndex, endIndex + 1);
		} else {
			// return original String if no brackets
			return fromAddress;
		}
	}

	public static void main(String[] args) {
		String[] testCases = { "foo@bar", "John Doe <foo@bar>", "[foo@bar]",
				"[John Doe <foo@bar>]", "[foo@bar", "foo@bar]" };
		for (String testCase : testCases) {
			System.out.println(testCase + " -> "
					+ stripBracketsFromFromAddress(testCase));
		}
	}
}
