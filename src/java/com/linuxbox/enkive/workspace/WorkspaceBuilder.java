package com.linuxbox.enkive.workspace;

/**
 * @author lee
 *
 */
public interface WorkspaceBuilder {

	
	/**
	 * Get a new workspace
	 * @return
	 */
	public Workspace getWorkspace();
	
	public Workspace getWorkspace(String workspaceUUID)
			throws WorkspaceException;

}
