/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

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
	protected SearchQueryBuilder searchQueryBuilder;

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	public MongoWorkspaceBuilder(MongoDbInfo workspaceInfo) {
		this.setWorkspaceCollection(workspaceInfo.getCollection());
	}

	public MongoWorkspaceBuilder(DBCollection workspaceColl,
			SearchQueryBuilder searchQueryBuilder) {
		this.workspaceColl = workspaceColl;
		this.searchQueryBuilder = searchQueryBuilder;
	}

	public void setWorkspaceCollection(DBCollection workspaceColl) {
		this.workspaceColl = workspaceColl;
	}

	public DBCollection getWorkspaceCollection() {
		return workspaceColl;
	}

	public SearchQueryBuilder getSearchQueryBuilder() {
		return searchQueryBuilder;
	}

	public void setSearchQueryBuilder(SearchQueryBuilder searchQueryBuilder) {
		this.searchQueryBuilder = searchQueryBuilder;
	}

	@Override
	public Workspace getWorkspace(String workspaceUUID)
			throws WorkspaceException {
		MongoWorkspace workspace = new MongoWorkspace();

		DBObject workspaceObject = workspaceColl.findOne(ObjectId
				.massageToObjectId(workspaceUUID));

		workspace.setWorkspaceCollection(workspaceColl);
		workspace.setSearchQueryBuilder(searchQueryBuilder);
		workspace.setWorkspaceUUID(workspaceUUID);
		workspace.setCreationDate((Date) workspaceObject
				.get(MongoWorkspaceConstants.CREATIONDATE));
		workspace.setLastUpdate((Date) workspaceObject
				.get(MongoWorkspaceConstants.MODIFIEDDATE));
		workspace.setCreator((String) workspaceObject
				.get(MongoWorkspaceConstants.CREATOR));
		workspace.setWorkspaceName((String) workspaceObject
				.get(MongoWorkspaceConstants.WORKSPACENAME));
		workspace.setLastQueryUUID((String) workspaceObject
				.get(MongoWorkspaceConstants.LASTQUERY));
//		workspace.setSearchFolderID((String) workspaceObject
//				.get(MongoWorkspaceConstants.SEARCHFOLDERID));
		BasicDBList searches = (BasicDBList) workspaceObject
				.get(MongoWorkspaceConstants.SEARCH_QUERIES);

		Collection<String> searchUUIDs = new HashSet<String>();
		Iterator<Object> searchesIterator = searches.iterator();
		while (searchesIterator.hasNext())
			searchUUIDs.add((String) searchesIterator.next());

		workspace.setSearchUUIDs(searchUUIDs);
		if (LOGGER.isInfoEnabled())
			LOGGER.info("Retrieved Workspace " + workspace.getWorkspaceName()
					+ " - " + workspace.getWorkspaceUUID());
		return workspace;
	}

	@Override
	public Workspace getWorkspace() {
		MongoWorkspace workspace = new MongoWorkspace();
		workspace.setWorkspaceCollection(workspaceColl);
		workspace.setSearchQueryBuilder(searchQueryBuilder);
		return workspace;
	}
}
