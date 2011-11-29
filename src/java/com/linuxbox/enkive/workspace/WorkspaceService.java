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

package com.linuxbox.enkive.workspace;

import java.util.List;
import java.util.Set;

import com.linuxbox.enkive.search.SearchProcess;
import com.linuxbox.util.threadpool.CancellableProcessExecutor.CPFuture;

public interface WorkspaceService extends WorkspaceServiceMBean {

	// NOTE: inherits getInteractiveSearchWaitSeconds() and
	// setInteractiveSearchWaitSeconds from WorkspaceServiceMBean

	Workspace getActiveWorkspace(String userId) throws WorkspaceException;

	Workspace getWorkspace(String workspaceUUID) throws WorkspaceException;

	String saveWorkspace(Workspace workspace) throws WorkspaceException;

	void deleteWorkspace(Workspace workspace) throws WorkspaceException;

	String saveSearchQuery(SearchQuery query) throws WorkspaceException;

	String saveSearchResult(SearchResult result) throws WorkspaceException;

	CPFuture<Set<String>> submitSearchProcessToQueue(SearchProcess process);

	void requestSearchCancellation(SearchResult result)
			throws WorkspaceException;

	List<SearchResult> getRecentSearches(String workspaceId)
			throws WorkspaceException;

	List<SearchResult> getSavedSearches(String workspaceId)
			throws WorkspaceException;

	SearchQuery getSearchQuery(String searchQueryId) throws WorkspaceException;

	void deleteSearchQuery(SearchQuery query) throws WorkspaceException;

	void deleteSearchQuery(String stringQueryId) throws WorkspaceException;

	SearchResult getSearchResult(String searchResultId)
			throws WorkspaceException;

	void deleteSearchResult(SearchResult result) throws WorkspaceException;

	void deleteSearchResult(String searchResultId) throws WorkspaceException;

}
