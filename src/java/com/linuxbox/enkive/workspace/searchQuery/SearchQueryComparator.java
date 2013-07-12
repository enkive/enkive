/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
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
package com.linuxbox.enkive.workspace.searchQuery;

import java.util.Comparator;
import static com.linuxbox.enkive.web.WebConstants.SORTBYSTATUS;
import static com.linuxbox.enkive.web.WebConstants.SORTBYNAME;
import static com.linuxbox.enkive.web.WebConstants.SORTBYDATE;

/**
 * Implementation of @ref Comparator for SearchQuery objects.  Can sort by Name,
 * Date, or Status
 * @author dang
 *
 */
public class SearchQueryComparator implements Comparator<SearchQuery> {

	String sortField;
	int sortDir;

	public SearchQueryComparator(String sortField, int sortDir) {
		this.sortField = sortField;
		this.sortDir = sortDir;
	}

	@Override
	public int compare(SearchQuery o1, SearchQuery o2) {
		// If we're sorting by status, sort by that first, then date
		int result = 0;

		if (sortField.equals(SORTBYSTATUS)) {
			result = o1.getStatus().compareTo(o2.getStatus());
		}

		else if (sortField.equals(SORTBYNAME)) {
			result = o1.getName().compareTo(o2.getName());
		}

		if (sortField.equals(SORTBYDATE) || result == 0) {
			if (o1.getTimestamp().after(o2.getTimestamp()))
				result = -1;
			else if (o1.getTimestamp().before(o2.getTimestamp()))
				result = 1;
		}

		return result * sortDir;
	}

}
