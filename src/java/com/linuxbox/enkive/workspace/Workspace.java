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

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * A Workspace represents a place where the search history is kept. It's kind of
 * like a shopping cart of searches. New searches are added to a workspace. Old
 * searches can be removed from workspaces, perhaps they're removed
 * automatically.
 * 
 * @author eric
 * 
 */
public class Workspace {

	protected String workspaceUUID;
	protected String workspaceName;
	protected String creator = "";
	protected Date creationDate;
	protected Date lastUpdate;
	protected Collection<String> searchResultUUIDs;

	public Workspace() {
		creationDate = new Date(System.currentTimeMillis());
		lastUpdate = creationDate;
		searchResultUUIDs = new HashSet<String>();
		workspaceName = DateFormat.getInstance().format(creationDate);
	}

	public String getWorkspaceUUID() {
		return workspaceUUID;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Collection<String> getSearchResultUUIDs() {
		return searchResultUUIDs;
	}

	public void setSearchResultUUIDs(Collection<String> searchResultUUIDs) {
		this.searchResultUUIDs = searchResultUUIDs;
	}

	public void addSearchResult(String searchResultUUID) {
		searchResultUUIDs.add(searchResultUUID);
	}

	public void deleteSearchResult(String searchResultUUID) {
		searchResultUUIDs.remove(searchResultUUID);
	}

	public void setWorkspaceUUID(String workspaceUUID) {
		this.workspaceUUID = workspaceUUID;
	}
}
