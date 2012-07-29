package com.linuxbox.enkive.workspace.searchFolder;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;

public abstract class SearchFolderSearchResult extends SearchResult {

	public SearchFolderSearchResult(){
		
	}
	
	public SearchFolderSearchResult(SearchResult searchResult){
		setExecutedBy(searchResult.getExecutedBy());
		setMessageIds(searchResult.getMessageIds());
		setSearchQueryBuilder(searchResult.getSearchQueryBuilder());
		setSearchQueryId(searchResult.getSearchQueryId());
		setTimestamp(searchResult.getTimestamp());
	}

	@Override
	public void sortSearchResultMessages(String sortBy, int sortDir)
			throws WorkspaceException {
		// TODO Auto-generated method stub

	}

}
