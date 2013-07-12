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
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * Implementation of @ref WorkspaceBuilder based on MongoDB.  @ref
 * MongoWorkspace objects are stored in a single collection in a
 * document-per-workspace.  The "_id" field is used as the internal workspace
 * ID.
 * @author dang
 *
 */
public class MongoWorkspaceBuilder implements WorkspaceBuilder {

	protected DBCollection workspaceColl;
	protected SearchResultBuilder searchResultBuilder;
	protected SearchFolderBuilder searchFolderBuilder;

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	public MongoWorkspaceBuilder(MongoClient m, String dbName,
			String workspaceCollName, SearchResultBuilder searchResultBuilder,
			SearchFolderBuilder searchFolderBuilder) {
		this(m.getDB(dbName).getCollection(workspaceCollName),
				searchResultBuilder, searchFolderBuilder);
	}

	public MongoWorkspaceBuilder(MongoDbInfo workspaceInfo,
			SearchResultBuilder searchResultBuilder,
			SearchFolderBuilder searchFolderBuilder) {
		this(workspaceInfo.getCollection(), searchResultBuilder,
				searchFolderBuilder);
	}

	public MongoWorkspaceBuilder(DBCollection workspaceColl,
			SearchResultBuilder searchResultBuilder,
			SearchFolderBuilder searchFolderBuilder) {
		this.workspaceColl = workspaceColl;
		this.searchResultBuilder = searchResultBuilder;
		this.searchFolderBuilder = searchFolderBuilder;
	}

	@Override
	public Workspace getWorkspace(String workspaceUUID)
			throws WorkspaceException {
		Workspace workspace = new MongoWorkspace(workspaceColl,
				searchResultBuilder);

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
		Workspace workspace = new MongoWorkspace(workspaceColl,
				searchResultBuilder);
		workspace.setSearchFolderBuilder(searchFolderBuilder);
		return workspace;
	}
}
