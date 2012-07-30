package com.linuxbox.enkive.workspace.searchFolder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;

public abstract class SearchFolder {

	protected String ID;
	protected Collection<SearchFolderSearchResult> results;
	protected SearchFolderSearchResultBuilder searchResultBuilder;

	public SearchFolder(SearchFolderSearchResultBuilder searchResultBuilder) {
		results = new HashSet<SearchFolderSearchResult>();
		this.searchResultBuilder = searchResultBuilder;
	}

	public String getID() {
		return ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public void addSearchResult(SearchResult searchResult)
			throws WorkspaceException {
		SearchFolderSearchResult searchFolderSearchResult = searchResultBuilder
				.buildSearchResult(searchResult);
		searchFolderSearchResult.saveSearchResult();
		results.add(searchFolderSearchResult);

	}

	public Collection<String> getMessageIds() {
		Collection<String> messageIds = new HashSet<String>();
		for (SearchFolderSearchResult result : results)
			messageIds.addAll(result.getMessageIds());
		return messageIds;
	}

	public void removeMessageId(String messageId) throws WorkspaceException {
		for (SearchFolderSearchResult result : results) {
			Set<String> folderMessageIds = result.getMessageIds();
			if (folderMessageIds.remove(messageId)) {
				if (folderMessageIds.isEmpty()) {
					result.deleteSearchResult();
					results.remove(result);
				} else {
					result.setMessageIds(folderMessageIds);
					result.saveSearchResult();
				}
			}
		}
	}

	public void removeMessageIds(Collection<String> messageIds)
			throws WorkspaceException {
		for (SearchFolderSearchResult result : results) {
			Set<String> folderMessageIds = result.getMessageIds();
			if (folderMessageIds.removeAll(messageIds)) {
				if (folderMessageIds.isEmpty()) {
					result.deleteSearchResult();
					results.remove(result);
				} else {
					result.setMessageIds(folderMessageIds);
					result.saveSearchResult();
				}
			}
		}
	}

	public abstract void saveSearchFolder();

}
