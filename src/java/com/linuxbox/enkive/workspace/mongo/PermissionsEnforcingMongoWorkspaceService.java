package com.linuxbox.enkive.workspace.mongo;

import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHQUERYID;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHRESULTS;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.UUID;

import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;

import com.linuxbox.enkive.authentication.AuthenticationException;
import com.linuxbox.enkive.authentication.AuthenticationService;
import com.linuxbox.enkive.permissions.PermissionService;
import com.linuxbox.enkive.workspace.SearchQuery;
import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class PermissionsEnforcingMongoWorkspaceService extends
		MongoWorkspaceService {

	protected PermissionService permService;
	protected AuthenticationService authenticationService;

	public PermissionsEnforcingMongoWorkspaceService(
			PermissionService permService, Mongo m, String dbName,
			String workspaceCollName, String searchResultsCollName,
			String searchQueryCollName, String userWorkspacesCollName) {
		super(m, dbName, workspaceCollName, searchResultsCollName,
				searchQueryCollName, userWorkspacesCollName);
		this.permService = permService;
	}

	public Workspace getActiveWorkspace() throws WorkspaceException {
		try {
			return getActiveWorkspace(authenticationService.getUserName());
		} catch (AuthenticationException e) {
			throw new WorkspaceException("Could not get active user", e);
		}
	}

	@Override
	public Workspace getWorkspace(String workspaceUUID)
			throws WorkspaceException {
		try {
			if (canReadWorkspace(authenticationService.getUserName(),
					workspaceUUID))
				return super.getWorkspace(workspaceUUID);
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
	public List<SearchResult> getRecentSearches(String workspaceId)
			throws WorkspaceException {
		try {
			if (canReadWorkspace(authenticationService.getUserName(),
					workspaceId))
				return super.getRecentSearches(workspaceId);
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
				return super.getSavedSearches(workspaceId);
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
	public SearchResult getSearchResult(String searchResultId)
			throws WorkspaceException {
		try {
			if (canReadSearchResult(authenticationService.getUserName(),
					searchResultId))
				return super.getSearchResult(searchResultId);
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
				super.deleteSearchResult(searchResultId);
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

	@Override
	public void deleteWorkspace(Workspace workspace) throws WorkspaceException {
		try {
			if (canDeleteWorkspace(authenticationService.getUserName(),
					workspace.getWorkspaceUUID()))
				super.deleteWorkspace(workspace);
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
	public SearchQuery getSearchQuery(String searchQueryId)
			throws WorkspaceException {
		try {
			if (canReadSearchQuery(authenticationService.getUserName(),
					searchQueryId))
				return super.getSearchQuery(searchQueryId);
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

	public boolean canReadWorkspace(String userId, String workspaceId) {
		DBObject workspaceObject = userWorkspacesColl.findOne(userId);
		BasicDBList workspaces = (BasicDBList) workspaceObject
				.get(MongoWorkspaceConstants.WORKSPACELIST);

		return workspaces.contains(workspaceId);

	}

	public boolean canSaveWorkspace(String userId, String workspaceId) {
		// TODO Does this need to be enforced?
		return true;
	}

	public boolean canDeleteWorkspace(String userId, String workspaceId) {
		// If we can read, we can delete
		return canReadWorkspace(userId, workspaceId);
	}

	public boolean canReadSearchQuery(String userId, String queryId)
			throws WorkspaceException {
		boolean canRead = false;
		DBObject workspaceObject = userWorkspacesColl.findOne(userId);
		BasicDBList workspaces = (BasicDBList) workspaceObject
				.get(MongoWorkspaceConstants.WORKSPACELIST);
		Iterator<Object> workspacesIterator = workspaces.iterator();
		while (workspacesIterator.hasNext()) {
			Workspace workspace = getWorkspace((String) workspacesIterator
					.next());
			workspace.getSearchResultUUIDs();

			BasicDBObject canReadQuery = new BasicDBObject();
			BasicDBList searchResultsQuery = new BasicDBList();
			for (String resultUUID : workspace.getSearchResultUUIDs()) {
				searchResultsQuery.add(new BasicDBObject(UUID, ObjectId
						.massageToObjectId((String) resultUUID)));
			}
			canReadQuery.put("$or", searchResultsQuery);
			canReadQuery.put(SEARCHQUERYID, queryId);
			DBCursor results = searchResultsColl.find(canReadQuery);
			if (results.count() > 0) {
				return true;
			}
		}
		return canRead;
	}

	public boolean canSaveSearchQuery(String userId, String queryId) {
		// TODO Does this need to be enforced?
		return true;
	}

	public boolean canDeleteSearchQuery(String userId, String queryId)
			throws WorkspaceException {
		// If we can read, we can delete
		return canReadSearchQuery(userId, queryId);
	}

	public boolean canReadSearchResult(String userId, String resultId) {
		DBObject workspaceObject = userWorkspacesColl.findOne(userId);
		BasicDBList workspaces = (BasicDBList) workspaceObject
				.get(MongoWorkspaceConstants.WORKSPACELIST);

		BasicDBObject canReadQuery = new BasicDBObject();
		BasicDBList workspacesQuery = new BasicDBList();
		Iterator<Object> workspacesIterator = workspaces.iterator();
		while (workspacesIterator.hasNext()) {
			workspacesQuery.add(new BasicDBObject(UUID, ObjectId
					.massageToObjectId((String) workspacesIterator.next())));
		}
		canReadQuery.put("$or", workspacesQuery);
		canReadQuery.put(SEARCHRESULTS, resultId);
		DBCursor results = workspaceColl.find(canReadQuery);
		return (results.count() > 0);
	}

	public boolean canSaveSearchResult(String userId, String resultId) {
		// TODO Does this need to be enforced?
		return true;
	}

	public boolean canDeleteSearchResult(String userId, String resultId) {
		// If we can read, we can delete
		return canReadSearchResult(userId, resultId);
	}

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	@Override
	public String saveWorkspace(Workspace workspace) throws WorkspaceException {
		// TODO Does this need to be permission enforced?
		return super.saveWorkspace(workspace);
	}

	@Override
	public String saveSearchQuery(SearchQuery query) throws WorkspaceException {
		String queryId = query.getId();
		try {
			if (canSaveSearchQuery(authenticationService.getUserName(), queryId))
				return super.saveSearchQuery(query);
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
				return super.saveSearchResult(result);
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
	public void deleteSearchQuery(SearchQuery query) throws WorkspaceException {
		deleteSearchQuery(query.getId());
	}

	@Override
	public void deleteSearchQuery(String stringQueryId)
			throws WorkspaceException {
		try {
			if (canDeleteSearchQuery(authenticationService.getUserName(),
					stringQueryId))
				super.deleteSearchQuery(stringQueryId);
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

}
