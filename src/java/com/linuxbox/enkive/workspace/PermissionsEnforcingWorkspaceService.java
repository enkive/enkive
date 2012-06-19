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
/*
 * 
 */
package com.linuxbox.enkive.workspace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.linuxbox.enkive.authentication.AuthenticationException;
import com.linuxbox.enkive.authentication.AuthenticationService;
import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.permissions.PermissionService;

public class PermissionsEnforcingWorkspaceService implements WorkspaceService {

	protected WorkspaceService workspaceService;
	protected AuthenticationService authenticationService;
	protected PermissionService permissionService;

	@Override
	public Workspace getActiveWorkspace(String userId)
			throws WorkspaceException {
		Workspace workspace = workspaceService.getActiveWorkspace(userId);
		try {
			if (canReadWorkspace(authenticationService.getUserName(),
					workspace.getWorkspaceUUID()))
				return workspace;
			else
				throw new WorkspaceException(
						"Could not get permissions to access workspace. User: "
								+ authenticationService.getUserName()
								+ " Workspace: " + workspace.getWorkspaceUUID());
		} catch (AuthenticationException e) {
			throw new WorkspaceException(
					"Could not determine user attempting to access workspace "
							+ workspace.getWorkspaceUUID());
		}
	}

	@Override
	public Workspace getWorkspace(String workspaceUUID)
			throws WorkspaceException {
		try {
			if (canReadWorkspace(authenticationService.getUserName(),
					workspaceUUID))
				return workspaceService.getWorkspace(workspaceUUID);
			else
				throw new WorkspaceException(
						"Could not get permissions to access workspace. User: "
								+ authenticationService.getUserName()
								+ " Workspace: " + workspaceUUID);
		} catch (AuthenticationException e) {
			throw new WorkspaceException(
					"Could not determine user attempting to access workspace "
							+ workspaceUUID);
		}
	}

	@Override
	public String saveWorkspace(Workspace workspace) throws WorkspaceException {
		try {
			if (canSaveWorkspace(authenticationService.getUserName(),
					workspace.getWorkspaceUUID()))
				return workspaceService.saveWorkspace(workspace);
			else
				throw new WorkspaceException("User "
						+ authenticationService.getUserName()
						+ " does not have permission to delete workspace "
						+ workspace.getWorkspaceUUID());
		} catch (AuthenticationException e) {
			throw new WorkspaceException(
					"Could not determine user attempting to delete workspace "
							+ workspace.getWorkspaceUUID());
		}
	}

	@Override
	public void deleteWorkspace(Workspace workspace) throws WorkspaceException {
		try {
			if (canDeleteWorkspace(authenticationService.getUserName(),
					workspace.getWorkspaceUUID()))
				workspaceService.deleteWorkspace(workspace);
			else
				throw new WorkspaceException("User "
						+ authenticationService.getUserName()
						+ " does not have permission to delete workspace "
						+ workspace.getWorkspaceUUID());
		} catch (AuthenticationException e) {
			throw new WorkspaceException(
					"Could not determine user attempting to delete workspace "
							+ workspace.getWorkspaceUUID());
		}
	}

	@Override
	public String saveSearchQuery(SearchQuery query) throws WorkspaceException {
		String queryId = query.getId();
		try {
			if (canSaveSearchQuery(authenticationService.getUserName(), queryId))
				return workspaceService.saveSearchQuery(query);
			else
				throw new WorkspaceException("User "
						+ authenticationService.getUserName()
						+ " does not have permission to save search query "
						+ queryId);
		} catch (AuthenticationException e) {
			throw new WorkspaceException(
					"Could not determine user attempting to save search query "
							+ queryId);
		}
	}

	@Override
	public String saveSearchResult(SearchResult result)
			throws WorkspaceException {
		String resultId = result.getId();
		try {
			if (canSaveSearchQuery(authenticationService.getUserName(),
					resultId))
				return workspaceService.saveSearchResult(result);
			else
				throw new WorkspaceException("User "
						+ authenticationService.getUserName()
						+ " does not have permission to save search result "
						+ resultId);
		} catch (AuthenticationException e) {
			throw new WorkspaceException(
					"Could not determine user attempting to save search result "
							+ resultId);
		}
	}

	@Override
	public List<SearchResult> getRecentSearches(String workspaceId)
			throws WorkspaceException {
		try {
			if (canReadWorkspace(authenticationService.getUserName(),
					workspaceId))
				return workspaceService.getRecentSearches(workspaceId);
			else
				throw new WorkspaceException("User "
						+ authenticationService.getUserName()
						+ " does not have permission to access workspace "
						+ workspaceId);
		} catch (AuthenticationException e) {
			throw new WorkspaceException(
					"Could not determine user attempting to access workspace "
							+ workspaceId);
		}

	}

	@Override
	public List<SearchResult> getSavedSearches(String workspaceId)
			throws WorkspaceException {
		try {
			if (canReadWorkspace(authenticationService.getUserName(),
					workspaceId))
				return workspaceService.getSavedSearches(workspaceId);
			else
				throw new WorkspaceException("User "
						+ authenticationService.getUserName()
						+ " does not have permission to access workspace "
						+ workspaceId);
		} catch (AuthenticationException e) {
			throw new WorkspaceException(
					"Could not determine user attempting to access workspace "
							+ workspaceId);
		}
	}

	@Override
	public SearchQuery getSearchQuery(String searchQueryId)
			throws WorkspaceException {
		try {
			if (canReadSearchQuery(authenticationService.getUserName(),
					searchQueryId))
				return workspaceService.getSearchQuery(searchQueryId);
			else
				throw new WorkspaceException("User "
						+ authenticationService.getUserName()
						+ " does not have permission to access search query "
						+ searchQueryId);
		} catch (AuthenticationException e) {
			throw new WorkspaceException(
					"Could not determine user attempting to access search query "
							+ searchQueryId);
		}
	}

	@Override
	public void deleteSearchQuery(SearchQuery query) throws WorkspaceException {
		deleteSearchQuery(query.getId());

	}

	@Override
	public void deleteSearchQuery(String stringQueryId)
			throws WorkspaceException {
		try {
			if (canDeleteSearchQuery(authenticationService.getUserName(),
					stringQueryId))
				workspaceService.deleteSearchQuery(stringQueryId);
			else
				throw new WorkspaceException("User "
						+ authenticationService.getUserName()
						+ " does not have permission to delete search query "
						+ stringQueryId);
		} catch (AuthenticationException e) {
			throw new WorkspaceException(
					"Could not determine user attempting to delete search query "
							+ stringQueryId);
		}
	}

	@Override
	public SearchResult getSearchResult(String searchResultId)
			throws WorkspaceException {
		try {
			if (canReadSearchResult(authenticationService.getUserName(),
					searchResultId))
				return workspaceService.getSearchResult(searchResultId);
			else
				throw new WorkspaceException("User "
						+ authenticationService.getUserName()
						+ " does not have permission to access search result "
						+ searchResultId);
		} catch (AuthenticationException e) {
			throw new WorkspaceException(
					"Could not determine user attempting to access search result "
							+ searchResultId);
		}
	}

	@Override
	public void deleteSearchResult(SearchResult result)
			throws WorkspaceException {
		deleteSearchResult(result.getId());

	}

	@Override
	public void deleteSearchResult(String searchResultId)
			throws WorkspaceException {
		try {
			if (canDeleteSearchResult(authenticationService.getUserName(),
					searchResultId))
				workspaceService.deleteSearchResult(searchResultId);
			else
				throw new WorkspaceException("User "
						+ authenticationService.getUserName()
						+ " does not have permission to delete search result "
						+ searchResultId);
		} catch (AuthenticationException e) {
			throw new WorkspaceException(
					"Could not determine user attempting to delete search result "
							+ searchResultId);
		}

	}

	public boolean canReadWorkspace(String userId, String workspaceId)
			throws WorkspaceException {
		try {
			if (permissionService.isAdmin())
				return true;
			Workspace workspace = workspaceService.getWorkspace(workspaceId);
			return (workspace.getCreator().equals(userId));
		} catch (CannotGetPermissionsException e) {
			throw new WorkspaceException(
					"Could not get permissions for user to access workspace", e);
		}

	}

	public boolean canSaveWorkspace(String userId, String workspaceId)
			throws WorkspaceException {
		// If we can read, we can save
		return canReadWorkspace(userId, workspaceId);
	}

	public boolean canDeleteWorkspace(String userId, String workspaceId)
			throws WorkspaceException {
		// If we can read, we can delete
		return canReadWorkspace(userId, workspaceId);
	}

	public boolean canReadSearchQuery(String userId, String queryId)
			throws WorkspaceException {
		try {
			if (permissionService.isAdmin())
				return true;
			Collection<Workspace> workspaces = getUserWorkspaces(userId);
			for (Workspace workspace : workspaces) {
				Collection<String> searchResultIds = workspace
						.getSearchResultUUIDs();
				for (String searchResultId : searchResultIds) {
					SearchResult searchResult = getSearchResult(searchResultId);
					if (searchResult.getSearchQueryId().equals(queryId))
						return true;
				}
			}
			return false;
		} catch (CannotGetPermissionsException e) {
			throw new WorkspaceException(
					"Could not get permissions for user to access workspace", e);
		}
	}

	public boolean canSaveSearchQuery(String userId, String queryId)
			throws WorkspaceException {
		// If we can read, we can save
		return canReadSearchQuery(userId, queryId);
	}

	public boolean canDeleteSearchQuery(String userId, String queryId)
			throws WorkspaceException {
		// If we can read, we can delete
		return canReadSearchQuery(userId, queryId);
	}

	public boolean canReadSearchResult(String userId, String resultId)
			throws WorkspaceException {
		try {
			if (permissionService.isAdmin())
				return true;
			Collection<Workspace> workspaces = getUserWorkspaces(userId);
			for (Workspace workspace : workspaces) {
				Collection<String> searchResultIds = workspace
						.getSearchResultUUIDs();
				if (searchResultIds.contains(resultId))
					return true;
			}
			return false;
		} catch (CannotGetPermissionsException e) {
			throw new WorkspaceException(
					"Could not get permissions for user to access workspace", e);
		}
	}

	public boolean canSaveSearchResult(String userId, String resultId)
			throws WorkspaceException {
		// If we can read, we can save
		return canReadSearchResult(userId, resultId);
	}

	public boolean canDeleteSearchResult(String userId, String resultId)
			throws WorkspaceException {
		// If we can read, we can delete
		return canReadSearchResult(userId, resultId);
	}

	public WorkspaceService getWorkspaceService() {
		return workspaceService;
	}

	public void setWorkspaceService(WorkspaceService workspaceService) {
		this.workspaceService = workspaceService;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	public Collection<Workspace> getUserWorkspaces(String userId)
			throws WorkspaceException {
		ArrayList<Workspace> workspaces = new ArrayList<Workspace>();
		for (Workspace workspace : workspaceService.getUserWorkspaces(userId))
			if (canReadWorkspace(userId, workspace.getWorkspaceUUID()))
				workspaces.add(workspace);
		return workspaces;
	}

	public PermissionService getPermissionService() {
		return permissionService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

}
