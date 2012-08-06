package com.linuxbox.enkive.workspace.searchFolder;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;

public interface SearchFolderSearchResultBuilder {

	SearchFolderSearchResult getSearchResult();
	
	SearchFolderSearchResult getSearchResult(String id) throws WorkspaceException;
	
	SearchFolderSearchResult buildSearchResult(SearchResult searchResult) throws WorkspaceException;
	
}
