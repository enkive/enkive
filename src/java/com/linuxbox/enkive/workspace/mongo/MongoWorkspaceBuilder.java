package com.linuxbox.enkive.workspace.mongo;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceBuilder;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderBuilder;
import com.linuxbox.enkive.workspace.searchResult.SearchResultBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoWorkspaceBuilder implements WorkspaceBuilder {

	protected Mongo m;
	protected DB workspaceDb;
	protected DBCollection workspaceColl;
	protected SearchResultBuilder searchResultBuilder;
	protected SearchFolderBuilder searchFolderBuilder;

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	public MongoWorkspaceBuilder(Mongo m, String dbName,
			String workspaceCollName, SearchResultBuilder searchResultBuilder,
			SearchFolderBuilder searchFolderBuilder) {
		this.m = m;
		workspaceDb = m.getDB(dbName);
		workspaceColl = workspaceDb.getCollection(workspaceCollName);
		this.searchResultBuilder = searchResultBuilder;
		this.searchFolderBuilder = searchFolderBuilder;
	}

	@Override
	public Workspace getWorkspace(String workspaceUUID)
			throws WorkspaceException {
		Workspace workspace = new MongoWorkspace(m, workspaceDb.getName(),
				workspaceColl.getName(), searchResultBuilder);

		DBObject workspaceObject = workspaceColl.findOne(ObjectId
				.massageToObjectId(workspaceUUID));

		workspace.setWorkspaceUUID(workspaceUUID);
		workspace.setCreationDate((Date) workspaceObject
				.get(MongoWorkspaceConstants.CREATIONDATE));
		workspace.setLastUpdate((Date) workspaceObject
				.get(MongoWorkspaceConstants.MODIFIEDDATE));
		workspace.setCreator((String) workspaceObject
				.get(MongoWorkspaceConstants.CREATOR));
		workspace.setWorkspaceName((String) workspaceObject
				.get(MongoWorkspaceConstants.WORKSPACENAME));
		workspace.setSearchFolderID((String) workspaceObject
				.get(MongoWorkspaceConstants.SEARCHFOLDERID));
		BasicDBList searchResults = (BasicDBList) workspaceObject
				.get(MongoWorkspaceConstants.SEARCHRESULTS);

		Collection<String> searchResultUUIDs = new HashSet<String>();
		Iterator<Object> searchResultsIterator = searchResults.iterator();
		while (searchResultsIterator.hasNext())
			searchResultUUIDs.add((String) searchResultsIterator.next());

		workspace.setSearchResultUUIDs(searchResultUUIDs);
		if (LOGGER.isInfoEnabled())
			LOGGER.info("Retrieved Workspace " + workspace.getWorkspaceName()
					+ " - " + workspace.getWorkspaceUUID());
		workspace.setSearchFolderBuilder(searchFolderBuilder);
		return workspace;
	}

	@Override
	public Workspace getWorkspace() {
		Workspace workspace = new MongoWorkspace(m, workspaceDb.getName(),
				workspaceColl.getName(), searchResultBuilder);
		workspace.setSearchFolderBuilder(searchFolderBuilder);
		return workspace;
	}

}
