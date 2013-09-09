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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryComparator;

/**
 * An abstract base class implementing Workspace objects.
 *
 * A Workspace represents a place where the search history is kept. It's kind of
 * like a shopping cart of searches. New searches are added to a workspace. Old
 * searches can be removed from workspaces, perhaps they're removed
 * automatically.  A workspace is owned by a single user, and each user has a
 * default workspace that is used if they have not explicitly selected one.
 *
 * This implements all workspace operations other than saving and deleting.
 * 
 * @author eric
 * 
 */
public abstract class Workspace {

	public static String SORTBYDATE = "sortByDate";
	public static String SORTBYSUBJECT = "sortBySubject";
	public static String SORTBYSENDER = "sortBySender";
	public static String SORTBYRECEIVER = "sortByReceiver";
	public static String SORTBYSTATUS = "sortByStatus";

	public static int SORT_ASC = 1;
	public static int SORT_DESC = -1;

	protected String workspaceUUID;
	protected String workspaceName;
	protected String creator = "";
	protected Date creationDate;
	protected Date lastUpdate;
	protected Collection<String> searchQueryUUIDs;
	protected String lastQueryUUID;
//	protected String searchFolderID;
	protected SearchQueryBuilder searchQueryBuilder;
//	protected SearchFolderBuilder searchFolderBuilder;

	public Workspace() {
		creationDate = new Date(System.currentTimeMillis());
		lastUpdate = creationDate;
		searchQueryUUIDs = new HashSet<String>();
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

/*	public String getSearchFolderID() throws WorkspaceException {
		// If we don't have a search folder id, assume we don't have a search
		// folder, and create one
		if (searchFolderID == null || searchFolderID.isEmpty())
			getSearchFolder();
		return searchFolderID;
	}*/

/*	public void setSearchFolderID(String searchFolderID) {
		this.searchFolderID = searchFolderID;
	}*/

	public Collection<String> getSearchUUIDs() {
		return searchQueryUUIDs;
	}

	public void setSearchUUIDs(Collection<String> searchQueryUUIDs) {
		this.searchQueryUUIDs = searchQueryUUIDs;
	}

	public void addSearch(String searchQueryUUID) {
		searchQueryUUIDs.add(searchQueryUUID);
	}

	public void deleteSearch(String searchQueryUUID) {
		searchQueryUUIDs.remove(searchQueryUUID);
	}

	public void addSearch(SearchQuery search) {
		searchQueryUUIDs.add(search.getId());
	}

	public void deleteSearch(SearchQuery search) {
		searchQueryUUIDs.remove(search.getId());
	}

	public void setWorkspaceUUID(String workspaceUUID) {
		this.workspaceUUID = workspaceUUID;
	}

	public String getLastQueryUUID() {
		return this.lastQueryUUID;
	}

	public void setLastQueryUUID(String lastQueryUUID) {
		this.lastQueryUUID = lastQueryUUID;
	}

	public List<SearchQuery> getSearches() throws WorkspaceException {
		return getSearches(SORTBYDATE, SORT_DESC);
	}

	public List<SearchQuery> getRecentSearches()
			throws WorkspaceException {
		return getSearches(SORTBYDATE, SORT_DESC);
	}

	public List<SearchQuery> getRecentSearches(String sortField,
			int sortDir) throws WorkspaceException {
		return getSearches(sortField, sortDir);
	}

	public List<SearchQuery> getSavedSearches() throws WorkspaceException {
		return getSavedSearches(SORTBYDATE, SORT_DESC);
	}

	public List<SearchQuery> getSavedSearches(String sortField,
			int sortDir) throws WorkspaceException {
		List<SearchQuery> searches = new ArrayList<SearchQuery>();
		for (SearchQuery search : getSearches(sortField, sortDir)) {
			if (search.isSaved())
				searches.add(search);
		}
		return searches;
	}

	public List<SearchQuery> getImapSearches() throws WorkspaceException {
		return getImapSearches(SORTBYDATE, SORT_DESC);
	}

	public List<SearchQuery> getImapSearches(String sortField,
			int sortDir) throws WorkspaceException {
		List<SearchQuery> searches = new ArrayList<SearchQuery>();
		for (SearchQuery search : getSearches(sortField, sortDir)) {
			if (search.isIMAP())
				searches.add(search);
		}
		return searches;
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

	protected List<SearchQuery> getSearches(String sortField, int sortDir)
			throws WorkspaceException {
		Collection<SearchQuery> searches = searchQueryBuilder
				.getSearchQueries(searchQueryUUIDs);
		return sortSearches(searches, sortField, sortDir);
	}

	public SearchQuery getSearch(String searchQueryUUID)
			throws WorkspaceException {
		return searchQueryBuilder.getSearchQuery(searchQueryUUID);
	}

	public static List<SearchQuery> sortSearches(Collection<SearchQuery> searches,
			String sortField, int sortDir) {
		List<SearchQuery> sortedSearches;
		sortedSearches = new ArrayList<SearchQuery>(searches);
		Collections.sort(sortedSearches, new SearchQueryComparator(
				sortField, sortDir));
		return sortedSearches;
	}

/*	public SearchFolder getSearchFolder() throws WorkspaceException {
		SearchFolder searchFolder;
		// Check to make sure we have a folder id, if not, assume we don't have
		// a folder and create one
		if (searchFolderID != null && !searchFolderID.isEmpty())
			searchFolder = searchFolderBuilder.getSearchFolder(searchFolderID);
		else {
			searchFolder = searchFolderBuilder.getSearchFolder();
			setSearchFolderID(searchFolder.getID());
			saveWorkspace();
		}
		return searchFolder;
	}*/

	public abstract void saveWorkspace() throws WorkspaceException;

	public abstract void deleteWorkspace() throws WorkspaceException;

}
