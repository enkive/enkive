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

	Workspace getActiveWorkspace() throws WorkspaceException;

	Workspace getActiveWorkspace(String userId) throws WorkspaceException;

	Workspace getWorkspace(String workspaceUUID) throws WorkspaceException;
	
	String saveWorkspace(Workspace workspace) throws WorkspaceException;
	
	void deleteWorkspace(Workspace workspace) throws WorkspaceException;
	
	void saveSearchQuery(SearchQuery query) throws WorkspaceException;

	
	
	SearchResult prepareQueryResultsRecord(SearchQuery query)
			throws WorkspaceException;

	CPFuture<Set<String>> submitSearchProcessToQueue(
			SearchProcess process);

	void requestSearchCancellation(SearchResult result)
			throws WorkspaceException;

	void setSearchResultStatus(SearchResult result, SearchResult.Status status)
			throws WorkspaceException;

	List<SearchQuery> readRecentSearches(Workspace workspace)
			throws WorkspaceException;

	List<SearchQuery> readSavedSearches(Workspace workspace)
			throws WorkspaceException;

	SearchQuery readQuery(String queryUUID)
			throws WorkspaceException;

	SearchResult readResult(String resultUUID)
			throws WorkspaceException;

	List<SearchResult> readResults(SearchQuery query) throws WorkspaceException;

	void deleteSearch(Workspace workspace, String id) throws WorkspaceException;
	
	void saveSearchWithName(Workspace workspace, String id, String name)
			throws WorkspaceException;
}
