package com.linuxbox.enkive.workspace.searchFolder;

import com.linuxbox.enkive.workspace.WorkspaceException;

public interface SearchFolderBuilder {

	public SearchFolder getSearchFolder();
	
	public SearchFolder getSearchFolder(String searchFolderId) throws WorkspaceException;
	
}
