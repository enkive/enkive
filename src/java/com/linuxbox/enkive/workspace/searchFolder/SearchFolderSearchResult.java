/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.workspace.searchFolder;

import java.util.List;

import com.linuxbox.enkive.workspace.searchResult.SearchResult;

public abstract class SearchFolderSearchResult extends SearchResult {

	public SearchFolderSearchResult() {
		super();
	}

	public SearchFolderSearchResult(SearchResult searchResult) {
		setMessageIds(searchResult.getMessageIds());
		setSearchQueryId(searchResult.getSearchQueryId());
	}

	@Override
	public List<String> getPage(String sortBy, int sortDir, int pageNum,
			int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}
}
