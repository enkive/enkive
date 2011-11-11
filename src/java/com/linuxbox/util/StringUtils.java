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

package com.linuxbox.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class StringUtils {
	// private final static Log LOGGER = LogFactory.getLog("com.linuxbox.util");

	public static final boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

	public static final boolean hasData(String s) {
		return s != null && !s.isEmpty();
	}

	public static String collectionToString(Collection<String> collection,
			String elementSeparator) {
		StringBuffer s = new StringBuffer();
		for (Iterator<String> i = collection.iterator(); i.hasNext();) {
			String s2 = i.next();
			s.append(s2);
			if (i.hasNext()) {
				s.append(elementSeparator);
			}
		}
		return s.toString();
	}

	public static String collectionToCommaSeparatedString(List<String> list) {
		return collectionToString(list, ", ");
	}

	public static String stringFromInputStream(InputStream input)
			throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		StreamConnector.transferForeground(input, output);
		try {
			output.close();
		} catch (IOException e) {
			// empty
		}
		return output.toString();
	}

	public static void main(String[] args) {
		String s1 = null;
		String s2 = "";
		String s3 = " ";

		System.out.println(isEmpty(s1));
		System.out.println(isEmpty(s2));
		System.out.println(isEmpty(s3));

		System.out.println(hasData(s1));
		System.out.println(hasData(s2));
		System.out.println(hasData(s3));
	}
}
