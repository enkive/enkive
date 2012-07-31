package com.linuxbox.enkive.workspace.mongo;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchResult.SearchResultBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoWorkspace extends Workspace {

	protected Mongo m;
	protected DB workspaceDb;
	protected DBCollection workspaceColl;

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspace.mongo");

	public MongoWorkspace(Mongo m, String dbName, String workspaceCollName,
			SearchResultBuilder searchResultBuilder) {
		this.m = m;
		workspaceDb = m.getDB(dbName);
		workspaceColl = workspaceDb.getCollection(workspaceCollName);
		this.searchResultBuilder = searchResultBuilder;

	}

	@Override
	public void saveWorkspace() throws WorkspaceException {
		BasicDBObject workspaceObject = new BasicDBObject();
		workspaceObject.put(MongoWorkspaceConstants.CREATIONDATE,
				getCreationDate());
		workspaceObject.put(MongoWorkspaceConstants.MODIFIEDDATE, new Date(
				System.currentTimeMillis()));
		workspaceObject.put(MongoWorkspaceConstants.CREATOR, getCreator());
		workspaceObject.put(MongoWorkspaceConstants.WORKSPACENAME,
				getWorkspaceName());
		workspaceObject.put(MongoWorkspaceConstants.SEARCHRESULTS,
				getSearchResultUUIDs());
		workspaceObject.put(MongoWorkspaceConstants.SEARCHFOLDERID,
				getSearchFolderID());

		if (getWorkspaceUUID() != null && !getWorkspaceUUID().isEmpty()) {
			DBObject toUpdate = workspaceColl.findOne(ObjectId
					.massageToObjectId(getWorkspaceUUID()));
			if (toUpdate != null) {
				workspaceColl.update(toUpdate, workspaceObject);
			}
		} else {
			workspaceColl.insert(workspaceObject);
			setWorkspaceUUID(workspaceObject
					.getString(MongoWorkspaceConstants.UUID));
		}

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Saved Workspace " + getWorkspaceName() + " - "
					+ getWorkspaceUUID());
	}

	@Override
	public void deleteWorkspace() throws WorkspaceException {
		DBObject workspaceObject = workspaceColl.findOne(ObjectId
				.massageToObjectId(getWorkspaceUUID()));
		workspaceColl.remove(workspaceObject);
	}

}
