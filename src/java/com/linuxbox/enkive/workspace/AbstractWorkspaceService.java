/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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

import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;
import com.linuxbox.util.MBeanUtils;

/**
 * Abstract base class for @ref WorkspaceService implementations.  This contains
 * some Builders, and implements the portions of the service that can be
 * delegated to such Builders.
 * @author dang
 *
 */
public abstract class AbstractWorkspaceService implements WorkspaceService {

	protected WorkspaceBuilder workspaceBuilder;
	protected SearchQueryBuilder searchQueryBuilder;
//	protected SearchFolderBuilder searchFolderBuilder;

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

	public SearchQueryBuilder getSearchQueryBuilder() {
		return searchQueryBuilder;
	}

	public void setSearchQueryBuilder(SearchQueryBuilder searchQueryBuilder) {
		this.searchQueryBuilder = searchQueryBuilder;
	}

/*	public SearchFolderBuilder getSearchFolderBuilder() {
		return searchFolderBuilder;
	}

	public void setSearchFolderBuilder(SearchFolderBuilder searchFolderBuilder) {
		this.searchFolderBuilder = searchFolderBuilder;
	}*/

	@Override
	public Workspace getWorkspace(String workspaceUUID)
			throws WorkspaceException {
		return workspaceBuilder.getWorkspace(workspaceUUID);
	}

	@Override
	public SearchQuery getSearch(String searchID)
			throws WorkspaceException {
		return searchQueryBuilder.getSearchQuery(searchID);
	}

/*	@Override
	public SearchFolder getSearchFolder(String searchFolderID)
			throws WorkspaceException {
		return searchFolderBuilder.getSearchFolder(searchFolderID);
	}*/
}