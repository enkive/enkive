package com.linuxbox.enkive.workspace.searchQuery;

import com.linuxbox.enkive.workspace.WorkspaceException;

public interface SearchQueryBuilder {

	public SearchQuery getSearchQuery() throws WorkspaceException;

	public SearchQuery getSearchQuery(String searchQueryId)
			throws WorkspaceException;

}
