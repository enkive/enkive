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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.linuxbox.enkive.workspace.searchFolder.SearchFolder;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderBuilder;
import com.linuxbox.enkive.workspace.searchResult.SearchResultComparator;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;
import com.linuxbox.enkive.workspace.searchResult.SearchResultBuilder;

/**
 * A Workspace represents a place where the search history is kept. It's kind of
 * like a shopping cart of searches. New searches are added to a workspace. Old
 * searches can be removed from workspaces, perhaps they're removed
 * automatically.
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
	protected Collection<String> searchResultUUIDs;
	protected String searchFolderID;
	protected SearchResultBuilder searchResultBuilder;
	protected SearchFolderBuilder searchFolderBuilder;

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

	public String getSearchFolderID() throws WorkspaceException {
		// If we don't have a search folder id, assume we don't have a search
		// folder, and create one
		if (searchFolderID == null || searchFolderID.isEmpty())
			getSearchFolder();
		return searchFolderID;
	}

	public void setSearchFolderID(String searchFolderID) {
		this.searchFolderID = searchFolderID;
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

	public void addSearchResult(SearchResult searchResult) {
		searchResultUUIDs.add(searchResult.getId());
	}

	public void deleteSearchResult(SearchResult searchResult) {
		searchResultUUIDs.remove(searchResult.getId());
	}

	public void setWorkspaceUUID(String workspaceUUID) {
		this.workspaceUUID = workspaceUUID;
	}

	public List<SearchResult> getSearchResults() {
		return getSearchResults(SORTBYDATE, SORT_DESC);
	}

	public List<SearchResult> getRecentSearchResults()
			throws WorkspaceException {
		return getSearchResults(SORTBYDATE, SORT_DESC);
	}

	public List<SearchResult> getRecentSearchResults(String sortField,
			int sortDir) throws WorkspaceException {
		return getSearchResults(sortField, sortDir);
	}

	public List<SearchResult> getSavedSearchResults() {
		return getSavedSearchResults(SORTBYDATE, SORT_DESC);
	}

	public List<SearchResult> getSavedSearchResults(String sortField,
			int sortDir) {
		List<SearchResult> searchResults = new ArrayList<SearchResult>();
		for (SearchResult searchResult : getSearchResults(sortField, sortDir)) {
			if (searchResult.isSaved())
				searchResults.add(searchResult);
		}
		return searchResults;
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

	protected List<SearchResult> getSearchResults(String sortField, int sortDir) {
		List<SearchResult> searchResults;
		Collection<SearchResult> searchResultColl = searchResultBuilder
				.getSearchResults(searchResultUUIDs);
		if (searchResultColl instanceof List)
			searchResults = (List<SearchResult>) searchResultColl;
		else
			searchResults = new ArrayList<SearchResult>(searchResultColl);
		return sortSearchResults(searchResults, sortField, sortDir);
	}

	public SearchResult getSearchResult(String searchResultId)
			throws WorkspaceException {
		return searchResultBuilder.getSearchResult(searchResultId);
	}

	public static List<SearchResult> sortSearchResults(
			Collection<SearchResult> searchResults, String sortField,
			int sortDir) {
		List<SearchResult> sortedSearchResults;
		sortedSearchResults = new ArrayList<SearchResult>(searchResults);
		Collections.sort(sortedSearchResults, new SearchResultComparator(
				sortField, sortDir));
		return sortedSearchResults;
	}

	public SearchFolder getSearchFolder() throws WorkspaceException {
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
	}

	public abstract void saveWorkspace() throws WorkspaceException;

	public abstract void deleteWorkspace() throws WorkspaceException;

}
