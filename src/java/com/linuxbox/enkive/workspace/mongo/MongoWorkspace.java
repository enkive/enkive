/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
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
 ******************************************************************************/
package com.linuxbox.enkive.workspace.mongo;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchResult.SearchResultBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoWorkspace extends Workspace {
	protected DBCollection workspaceColl;

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspace.mongo");

	public MongoWorkspace(DBCollection workspaceColl,
			SearchResultBuilder searchResultBuilder) {
		this.workspaceColl = workspaceColl;
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
