package com.linuxbox.enkive.web.search;

import java.util.List;

import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;

public class SavedSearchListServlet extends AbstractSearchListServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6786720212005301805L;

	@Override
	List<SearchResult> getSearches(Workspace workspace)
			throws WorkspaceException {
		return workspaceService.getSavedSearches(workspace.getWorkspaceUUID());
	}



}