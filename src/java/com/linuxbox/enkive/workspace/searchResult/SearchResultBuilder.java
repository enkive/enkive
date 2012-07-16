package com.linuxbox.enkive.workspace.searchResult;

import java.util.Collection;

import com.linuxbox.enkive.workspace.WorkspaceException;

public interface SearchResultBuilder {

	SearchResult getSearchResult() throws WorkspaceException;

	SearchResult getSearchResult(String searchResultId)
			throws WorkspaceException;

	Collection<SearchResult> getSearchResults(
			Collection<String> searchResultUUIDs);

}
