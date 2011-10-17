/*
 *  Copyright 2011 The Linux Box Corporation.
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

package com.linuxbox.enkive.search;

import java.util.Set;

import com.linuxbox.enkive.message.search.MessageSearchSummary;
import com.linuxbox.enkive.workspace.SearchQuery;
import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.WorkspaceService;
import com.linuxbox.util.threadpool.AbstractCancellableProcess;

public abstract class SearchProcess extends
		AbstractCancellableProcess<Set<String>> {
	protected final WorkspaceService workspaceService;

	protected final MessageSearchSummary searchSummary;
	protected final String searchUser;

	protected final SearchQuery searchQuery;
	protected final SearchResult searchResult;

	public SearchProcess(WorkspaceService workspaceServiceParam,
			MessageSearchSummary searchSummary, String searchUser,
			SearchQuery searchQuery, SearchResult searchResult) {
		super(searchResult.getId());

		this.workspaceService = (WorkspaceService) workspaceServiceParam;

		this.searchSummary = searchSummary;
		this.searchUser = searchUser;

		this.searchQuery = searchQuery;
		this.searchResult = searchResult;
	}

	public SearchQuery getSearchQuery() {
		return searchQuery;
	}

	public SearchResult getSearchResult() {
		return searchResult;
	}
}