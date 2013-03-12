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
package com.linuxbox.enkive.workspace.searchResult;

import java.util.Comparator;

import com.linuxbox.enkive.workspace.WorkspaceException;

public class SearchResultComparator implements Comparator<SearchResult> {

	String sortField;
	int sortDir;

	public SearchResultComparator(String sortField, int sortDir) {
		this.sortField = sortField;
		this.sortDir = sortDir;
	}

	@Override
	public int compare(SearchResult o1, SearchResult o2) {
		// If we're sorting by status, sort by that first, then date
		int result = 0;

		if (sortField.equals(SearchResult.SORTBYSTATUS)) {
			result = o1.getStatus().compareTo(o2.getStatus());
		}

		else if (sortField.equals(SearchResult.SORTBYNAME)) {
			try {
				result = o1.getSearchQuery().getName()
						.compareTo(o2.getSearchQuery().getName());
			} catch (WorkspaceException e) {
				result = 0;
			}
		}

		if (sortField.equals(SearchResult.SORTBYDATE) || result == 0) {
			if (o1.getTimestamp().after(o2.getTimestamp()))
				result = -1;
			else if (o1.getTimestamp().before(o2.getTimestamp()))
				result = 1;
		}

		return result * sortDir;
	}

}
