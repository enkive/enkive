package com.linuxbox.enkive.workspace.mongo;

import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.ACTIVEWORKSPACE;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.CREATIONDATE;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.CREATOR;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.MODIFIEDDATE;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHRESULTS;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.UUID;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.WORKSPACENAME;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.*;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
	protected DBCollection searchResultsColl;
	protected DBCollection searchQueryColl;
	protected DBCollection userWorkspacesColl;

	private AuthenticationService authenticationService;

	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	public MongoWorkspaceService(Mongo m, String dbName,
			String workspaceCollName, String searchResultsCollName,
			String searchQueryCollName, String userWorkspacesCollName) {
		this.m = m;
		workspaceDb = m.getDB(dbName);
		workspaceColl = workspaceDb.getCollection(workspaceCollName);
		searchResultsColl = workspaceDb.getCollection(searchResultsCollName);
		searchQueryColl = workspaceDb.getCollection(searchQueryCollName);
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
		DBObject workspaceObject = workspaceColl.findOne(ObjectId
				.massageToObjectId(workspaceUUID));

		workspace.setWorkspaceUUID(workspaceUUID);
		workspace.setCreationDate((Date) workspaceObject.get(CREATIONDATE));
		workspace.setLastUpdate((Date) workspaceObject.get(MODIFIEDDATE));
		workspace.setCreator((String) workspaceObject.get(CREATOR));
		workspace.setWorkspaceName((String) workspaceObject.get(WORKSPACENAME));
		BasicDBList searchResults = (BasicDBList) workspaceObject
				.get(SEARCHRESULTS);

		Collection<String> searchResultUUIDs = new HashSet<String>();
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
				&& !workspace.getWorkspaceUUID().isEmpty()) {
			DBObject toUpdate = workspaceColl.findOne(ObjectId
					.massageToObjectId(workspace.getWorkspaceUUID()));
			if (toUpdate != null) {
				workspaceColl.update(toUpdate, workspaceObject);
				workspaceObject.put(UUID, toUpdate.get(UUID));
			}
		}
		if (workspaceObject.getString(UUID) == null)
			workspaceColl.insert(workspaceObject);

		logger.info("Saved Workspace " + workspace.getWorkspaceName() + " - "
				+ workspaceObject.getString(UUID));
		return workspaceObject.getString(UUID);
	}

	@Override
	public void deleteWorkspace(Workspace workspace) throws WorkspaceException {
		// TODO Auto-generated method stub

	}

	@Override
	public String saveSearchQuery(SearchQuery query) throws WorkspaceException {
		BasicDBObject searchQueryObject = new BasicDBObject();
		searchQueryObject.put(SEARCHNAME, query.getName());
		searchQueryObject.put(SEARCHCRITERIA, query.getCriteria());

		if (query.getId() != null && !query.getId().isEmpty()) {
			DBObject toUpdate = searchQueryColl.findOne(ObjectId
					.massageToObjectId(query.getId()));
			if (toUpdate != null) {
				searchResultsColl.update(toUpdate, searchQueryObject);
				searchQueryObject.put(UUID, toUpdate.get(UUID));
			}
		}
		if (searchQueryObject.getString(UUID) == null)
			searchQueryColl.insert(searchQueryObject);

		logger.info("Saved Search Results - "
				+ searchQueryObject.getString(UUID));
		return searchQueryObject.getString(UUID);

	}

	@Override
	public SearchQuery getSearchQuery(String searchQueryId)
			throws WorkspaceException {
		SearchQuery query = new SearchQuery();
		DBObject queryObject = searchQueryColl.findOne(ObjectId
				.massageToObjectId(searchQueryId));

		query.setId(searchQueryId);
		query.setName((String) queryObject.get(SEARCHNAME));
		query.setCriteria(((BasicDBObject) queryObject.get(SEARCHCRITERIA))
				.toMap());

		logger.info("Retrieved Search Query " + query.getName() + " - "
				+ query.getId());
		return query;
	}

	@Override
	public void deleteSearchQuery(SearchQuery query) throws WorkspaceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteSearchQuery(String stringQueryId)
			throws WorkspaceException {
		// TODO Auto-generated method stub

	}

	@Override
	public String saveSearchResult(SearchResult result)
			throws WorkspaceException {

		BasicDBObject searchResultObject = new BasicDBObject();
		searchResultObject.put(EXECUTIONTIMESTAMP, result.getTimestamp());
		searchResultObject.put(EXECUTEDBY, result.getExecutedBy());
		searchResultObject.put(SEARCHRESULTS, result.getMessageIds());
		searchResultObject.put(SEARCHSTATUS, result.getStatus().toString());
		searchResultObject.put(SEARCHQUERYID, result.getSearchQueryId());

		if (result.getId() != null && !result.getId().isEmpty()) {
			DBObject toUpdate = searchResultsColl.findOne(ObjectId
					.massageToObjectId(result.getId()));
			if (toUpdate != null) {
				searchResultsColl.update(toUpdate, searchResultObject);
				searchResultObject.put(UUID, toUpdate.get(UUID));
			}
		}
		if (searchResultObject.getString(UUID) == null)
			searchResultsColl.insert(searchResultObject);

		logger.info("Saved Search Results - "
				+ searchResultObject.getString(UUID));
		return searchResultObject.getString(UUID);

	}

	@Override
	public SearchResult getSearchResult(String searchResultId)
			throws WorkspaceException {
		SearchResult result = new SearchResult();
		DBObject searchResultObject = searchResultsColl.findOne(ObjectId
				.massageToObjectId(searchResultId));

		result.setId(searchResultId);
		result.setTimestamp((Date) searchResultObject.get(EXECUTIONTIMESTAMP));
		result.setExecutedBy((String) searchResultObject.get(EXECUTEDBY));

		BasicDBList searchResults = (BasicDBList) searchResultObject
				.get(SEARCHRESULTS);

		Collection<String> searchResultUUIDs = new HashSet<String>();
		Iterator<Object> searchResultsIterator = searchResults.iterator();
		while (searchResultsIterator.hasNext())
			searchResultUUIDs.add((String) searchResultsIterator.next());

		result.setMessageIds(searchResultUUIDs);

		result.setStatus(Status.valueOf((String) searchResultObject
				.get(SEARCHSTATUS)));
		result.setSearchQueryId((String) searchResultObject.get(SEARCHQUERYID));

		logger.info("Retrieved Search Results - " + result.getId());
		return result;
	}

	@Override
	public void deleteSearchResult(SearchResult result)
			throws WorkspaceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteSearchResult(String searchResultId)
			throws WorkspaceException {
		// TODO Auto-generated method stub

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

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public DBCollection getuserWorkspacesColl() {
		return userWorkspacesColl;
	}

	public void setuserWorkspacesCollColl(DBCollection userWorkspacesColl) {
		this.userWorkspacesColl = userWorkspacesColl;
	}

	public DBCollection getWorkspaceColl() {
		return workspaceColl;
	}

	public void setWorkspaceColl(DBCollection workspaceColl) {
		this.workspaceColl = workspaceColl;
	}

	public DBCollection getSearchColl() {
		return searchResultsColl;
	}

	public void setSearchColl(DBCollection searchColl) {
		this.searchResultsColl = searchColl;
	}

}
