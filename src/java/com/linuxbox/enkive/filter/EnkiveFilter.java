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

package com.linuxbox.enkive.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.field.address.AddressList;
import org.apache.james.mime4j.field.address.parser.ParseException;

public class EnkiveFilter {
	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.filter");

	private String header;
	private int filterType;
	private String filterValue;

	private EnkiveFilter(String header, int filterType) {
		this.header = header;
		this.filterType = filterType;
	}

	public EnkiveFilter(String header, int filterType, String filterValue) {
		this(header, filterType);
		this.filterValue = filterValue;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getHeader() {
		return header;
	}

	public void setFilterType(int filterType) {
		this.filterType = filterType;
	}

	public int getFilterType() {
		return filterType;
	}

	public boolean filter(String value) {
		boolean passed = true;
		switch (filterType) {
		case EnkiveFilterType.NUMERICAL:
			passed = new Float(value) < new Float(filterValue);
			break;
		case EnkiveFilterType.TEXT:
			passed = !filterValue.equals(value);
			break;
		case EnkiveFilterType.ADDRESS:
			try {
				AddressList addresses = AddressList.parse(value);
				if (addresses.size() == 1
						&& addresses.get(0).toString().contains(filterValue))
					passed = false;
			} catch (ParseException e) {
				logger.warn("Could not parse Address list for filtering");
				passed = true;
			}
			break;
		}
		return passed;
	}
}
