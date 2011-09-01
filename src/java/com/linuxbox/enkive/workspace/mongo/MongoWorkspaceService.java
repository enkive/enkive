package com.linuxbox.enkive.workspace.mongo;

import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.ACTIVEWORKSPACE;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.CREATIONDATE;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.CREATOR;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.MODIFIEDDATE;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHRESULTS;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.UUID;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.WORKSPACENAME;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.authentication.AuthenticationException;
import com.linuxbox.enkive.authentication.AuthenticationService;
import com.linuxbox.enkive.workspace.AbstractWorkspaceService;
import com.linuxbox.enkive.workspace.SearchQuery;
import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.SearchResult.Status;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceServiceMBean;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoWorkspaceService extends AbstractWorkspaceService implements
		WorkspaceServiceMBean {

	protected Mongo m = null;
	protected DB workspaceDb;
	protected DBCollection workspaceColl;
	protected DBCollection searchColl;
	protected DBCollection userWorkspacesColl;

	private AuthenticationService authenticationService;

	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	public MongoWorkspaceService(Mongo m, String dbName,
			String workspaceCollName, String searchCollName,
			String userWorkspacesCollName) {
		this.m = m;
		workspaceDb = m.getDB(dbName);
		workspaceColl = workspaceDb.getCollection(workspaceCollName);
		searchColl = workspaceDb.getCollection(searchCollName);
		userWorkspacesColl = workspaceDb.getCollection(userWorkspacesCollName);
	}

	@Override
	public Workspace getActiveWorkspace() throws WorkspaceException {
		try {
			return getActiveWorkspace(authenticationService.getUserName());
		} catch (AuthenticationException e) {
			throw new WorkspaceException("Could not get active user", e);
		}
	}

	@Override
	public Workspace getActiveWorkspace(String userId)
			throws WorkspaceException {
		DBObject workspaceList = userWorkspacesColl.findOne(userId);
		String workspaceUUID = (String) workspaceList.get(ACTIVEWORKSPACE);
		return getWorkspace(workspaceUUID);
	}

	@Override
	public Workspace getWorkspace(String workspaceUUID)
			throws WorkspaceException {
		Workspace workspace = new Workspace();
		DBObject workspaceObject = workspaceColl.findOne(ObjectId.massageToObjectId(workspaceUUID));

		workspace.setWorkspaceUUID(workspaceUUID);
		workspace.setCreationDate((Date) workspaceObject.get(CREATIONDATE));
		workspace.setLastUpdate((Date) workspaceObject.get(MODIFIEDDATE));
		workspace.setCreator((String) workspaceObject.get(CREATOR));
		workspace.setWorkspaceName((String) workspaceObject.get(WORKSPACENAME));
		BasicDBList searchResults = (BasicDBList) workspaceObject
				.get(SEARCHRESULTS);
		Set<String> searchResultUUIDs = new HashSet<String>();
		Iterator<Object> searchResultsIterator = searchResults.iterator();
		while (searchResultsIterator.hasNext())
			searchResultUUIDs.add((String) searchResultsIterator.next());

		workspace.setSearchResultUUIDs(searchResultUUIDs);
		logger.info("Retrieved Workspace " + workspace.getWorkspaceName()
				+ " - " + workspace.getWorkspaceUUID());
		return workspace;
	}

	@Override
	public String saveWorkspace(Workspace workspace) throws WorkspaceException {
		BasicDBObject workspaceObject = new BasicDBObject();
		workspaceObject.put(CREATIONDATE, workspace.getCreationDate());
		workspaceObject.put(MODIFIEDDATE, new Date(System.currentTimeMillis()));
		workspaceObject.put(CREATOR, workspace.getCreator());
		workspaceObject.put(WORKSPACENAME, workspace.getWorkspaceName());
		workspaceObject.put(SEARCHRESULTS, workspace.getSearchResultUUIDs());

		if (workspace.getWorkspaceUUID() != null
				&& !workspace.getWorkspaceUUID().isEmpty()){
			DBObject toUpdate = workspaceColl.findOne(ObjectId.massageToObjectId(workspace.getWorkspaceUUID()));
			if(toUpdate != null){
				workspaceColl.update(toUpdate, workspaceObject);
				workspaceObject.put(UUID, toUpdate.get(UUID));
			}
		}
		if(workspaceObject.getString(UUID) != null)
			workspaceColl.insert(workspaceObject);
			
		logger.info("Saved Workspace " + workspace.getWorkspaceName() + " - "
				+ workspaceObject.getString(UUID));
		return workspaceObject.getString(UUID);
	}

	@Override
	public void saveSearchQuery(SearchQuery query) throws WorkspaceException {
		// TODO Auto-generated method stub

	}

	@Override
	public SearchResult prepareQueryResultsRecord(SearchQuery query)
			throws WorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSearchResultStatus(SearchResult result, Status status)
			throws WorkspaceException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<SearchQuery> readRecentSearches(Workspace workspace)
			throws WorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchQuery> readSavedSearches(Workspace workspace)
			throws WorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchResult> readResults(SearchQuery query)
			throws WorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteSearch(Workspace workspace, String id)
			throws WorkspaceException {
		// TODO Auto-generated method stub

	}

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public DBCollection getPermissionsColl() {
		return userWorkspacesColl;
	}

	public void setPermissionsColl(DBCollection permissionsColl) {
		this.userWorkspacesColl = permissionsColl;
	}

	public DBCollection getWorkspaceColl() {
		return workspaceColl;
	}

	public void setWorkspaceColl(DBCollection workspaceColl) {
		this.workspaceColl = workspaceColl;
	}

	public DBCollection getSearchColl() {
		return searchColl;
	}

	public void setSearchColl(DBCollection searchColl) {
		this.searchColl = searchColl;
	}

	@Override
	public void deleteWorkspace(Workspace workspace) throws WorkspaceException {
		// TODO Auto-generated method stub

	}

	@Override
	public SearchQuery readQuery(String queryUUID) throws WorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchResult readResult(String resultUUID) throws WorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveSearchWithName(Workspace workspace, String id, String name)
			throws WorkspaceException {
		// TODO Auto-generated method stub

	}

}
