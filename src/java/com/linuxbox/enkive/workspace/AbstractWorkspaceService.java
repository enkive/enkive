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
package com.linuxbox.enkive.workspace;

import com.linuxbox.enkive.workspace.searchFolder.SearchFolder;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderBuilder;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;
import com.linuxbox.enkive.workspace.searchResult.SearchResultBuilder;
import com.linuxbox.util.MBeanUtils;

public abstract class AbstractWorkspaceService implements WorkspaceService {

	protected WorkspaceBuilder workspaceBuilder;
	protected SearchResultBuilder searchResultBuilder;
	protected SearchFolderBuilder searchFolderBuilder;

	public AbstractWorkspaceService() {

	}

	public void registerMBean() {
		final String type = getClass().getSimpleName();
		final String name = "the service";
		MBeanUtils.registerMBean(this, WorkspaceServiceMBean.class, type, name);
	}

	public WorkspaceBuilder getWorkspaceBuilder() {
		return workspaceBuilder;
	}

	public void setWorkspaceBuilder(WorkspaceBuilder workspaceBuilder) {
		this.workspaceBuilder = workspaceBuilder;
	}

	public SearchResultBuilder getSearchResultBuilder() {
		return searchResultBuilder;
	}

	public void setSearchResultBuilder(SearchResultBuilder searchResultBuilder) {
		this.searchResultBuilder = searchResultBuilder;
	}

	public SearchFolderBuilder getSearchFolderBuilder() {
		return searchFolderBuilder;
	}

	public void setSearchFolderBuilder(SearchFolderBuilder searchFolderBuilder) {
		this.searchFolderBuilder = searchFolderBuilder;
	}

	@Override
	public Workspace getWorkspace(String workspaceUUID)
			throws WorkspaceException {
		return workspaceBuilder.getWorkspace(workspaceUUID);
	}

	@Override
	public SearchResult getSearchResult(String searchResultID)
			throws WorkspaceException {
		return searchResultBuilder.getSearchResult(searchResultID);
	}

	@Override
	public SearchFolder getSearchFolder(String searchFolderID)
			throws WorkspaceException {
		return searchFolderBuilder.getSearchFolder(searchFolderID);
	}
}