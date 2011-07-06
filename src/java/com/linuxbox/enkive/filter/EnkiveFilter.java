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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.field.address.Address;
import org.apache.james.mime4j.field.address.AddressList;
import org.apache.james.mime4j.field.address.parser.ParseException;

import com.linuxbox.enkive.filter.EnkiveFilterConstants.FilterAction;
import com.linuxbox.enkive.filter.EnkiveFilterConstants.FilterComparator;
import com.linuxbox.enkive.filter.EnkiveFilterConstants.FilterType;

public class EnkiveFilter {
	
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
	"EEE, dd MMM yyyy HH:mm:ss ZZZZ");
	
	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.filter");

	private String header;
	private int filterAction;
	private int filterType;
	private int filterComparator;
	private String filterValue;
	private int defaultAction;

	private EnkiveFilter(String header, int filterType) {
		this.header = header;
		this.filterType = filterType;
	}

	public EnkiveFilter(String header, int filterAction, int filterType,
			String filterValue, int filterComparator, int defaultAction) {
		this(header, filterType);
		this.filterAction = filterAction;
		this.filterValue = filterValue;
		this.filterComparator = filterComparator;
		this.defaultAction = defaultAction;
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

	public int getFilterAction() {
		return filterAction;
	}

	public void setFilterAction(int filterAction) {
		this.filterAction = filterAction;
	}

	public boolean filter(String value) {
		boolean matched = true;
		switch (filterType) {
			case FilterType.INTEGER:
				matched = filterInteger(value);
				break;
			case FilterType.FLOAT:
				matched = filterFloat(value);
				break;
			case FilterType.DATE:
				try {
					matched = filterDate(value);
				} catch (java.text.ParseException e) {
					logger.warn("Could not parse Date for filtering", e);
					matched = false;
				}
				break;
			case FilterType.STRING:
				matched = filterString(value);
				break;
			case FilterType.ADDRESS:
				try {
					matched = filterAddress(value);
				} catch (ParseException e) {
					logger.warn("Could not parse Address list for filtering", e);
					matched = false;
				}
				break;
		}
		if (matched && filterAction == FilterAction.ALLOW)
			return true;
		else if (matched && filterAction == FilterAction.DENY)
			return false;
		else if (defaultAction == FilterAction.DENY)
			return false;
		else
			return true;
	}

	private boolean filterString(String value) {
		boolean matched = false;
		switch (filterComparator) {
			case FilterComparator.MATCHES:
				if (value.equals(filterValue))
					matched = true;
				break;
			case FilterComparator.DOES_NOT_MATCH:
				if (!value.equals(filterValue))
					matched = true;
				break;
		}
		return matched;
	}

	private boolean filterDate(String value) throws java.text.ParseException {
		boolean matched = false;
		
		Date dateValue = dateFormatter.parse(value);
		Date dateFilterValue = dateFormatter.parse(filterValue);

		switch (filterComparator) {
			case FilterComparator.MATCHES:
				if (value.equals(filterValue))
					matched = true;
				break;
			case FilterComparator.DOES_NOT_MATCH:
				if (!value.equals(filterValue))
					matched = true;
				break;
			case FilterComparator.IS_GREATER_THAN:
				if (dateValue.after(dateFilterValue))
					matched = true;
				break;
			case FilterComparator.IS_LESS_THAN:
				if (dateValue.before(dateFilterValue))
					matched = true;
				break;
		}
		return matched;
	}

	private boolean filterFloat(String value) {
		boolean matched = false;

		float floatValue = Float.valueOf(value);
		float floatFilterValue = Float.valueOf(filterValue);

		switch (filterComparator) {
		case FilterComparator.MATCHES:
			if (value.equals(filterValue))
				matched = true;
			break;
		case FilterComparator.DOES_NOT_MATCH:
			if (!value.equals(filterValue))
				matched = true;
			break;
		case FilterComparator.IS_GREATER_THAN:
			if (floatValue > floatFilterValue)
				matched = true;
			break;
		case FilterComparator.IS_LESS_THAN:
			if (floatValue < floatFilterValue)
				matched = true;
			break;

		}
		return matched;
	}

	private boolean filterInteger(String value) {
		boolean matched = false;

		int intValue = Integer.valueOf(value);
		int intFilterValue = Integer.valueOf(filterValue);

		switch (filterComparator) {
		case FilterComparator.MATCHES:
			if (value.equals(filterValue))
				matched = true;
			break;
		case FilterComparator.DOES_NOT_MATCH:
			if (!value.equals(filterValue))
				matched = true;
			break;
		case FilterComparator.IS_GREATER_THAN:
			if (intValue > intFilterValue)
				matched = true;
			break;
		case FilterComparator.IS_LESS_THAN:
			if (intValue < intFilterValue)
				matched = true;
			break;
		}
		return matched;
	}

	private boolean filterAddress(String value) throws ParseException {
		boolean matched = false;
		AddressList addresses = AddressList.parse(value);
		Address address = Address.parse(filterValue);
		
		switch (filterComparator) {
			case FilterComparator.MATCHES:
				if (addresses.size() == 1 && addresses.get(0).equals(address))
					matched = true;
				break;
			case FilterComparator.DOES_NOT_MATCH:
				if (addresses.size() == 1 && !addresses.get(0).equals(address))
					matched = true;
				break;
			case FilterComparator.CONTAINS:
				if (addresses.contains(address))
					matched = true;
				break;
			case FilterComparator.DOES_NOT_CONTAIN:
				if (!addresses.contains(address))
					matched = true;
				break;
		}
		return matched;
	}
}
