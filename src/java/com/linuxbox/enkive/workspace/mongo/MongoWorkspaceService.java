/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
package com.linuxbox.enkive.workspace.mongo;

import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.ACTIVEWORKSPACE;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.UUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.workspace.AbstractWorkspaceService;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceServiceMBean;
import com.linuxbox.enkive.workspace.searchResult.mongo.MongoSearchResultUtils;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * A @ref WorkspaceService implementation based on MongoDB.  Workspace UUIDs are
 * stored in a Collection on a per-user basis, where each user has a Document
 * consisting of a list of workspaces and the current active workspace.
 * @ref Workspace objects themselves are handled by the @ref WorkspaceBuilder
 *
 * @author dang
 *
 */
public class MongoWorkspaceService extends AbstractWorkspaceService implements
		WorkspaceServiceMBean {

	protected DBCollection userWorkspacesColl;
	protected AuditService auditService;

	protected MongoSearchResultUtils searchResultUtils;

	@SuppressWarnings("unused")
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspaces.mongo");

	public MongoWorkspaceService(MongoClient m, String dbName,
			String userWorkspacesCollName) {
		DB workspaceDb = m.getDB(dbName);
		userWorkspacesColl = workspaceDb.getCollection(userWorkspacesCollName);
	}
	
	public MongoWorkspaceService(MongoDbInfo info) {
		userWorkspacesColl = info.getCollection();
	}

	@Override
	public Workspace getActiveWorkspace(String userId)
			throws WorkspaceException {
		DBObject workspaceList = userWorkspacesColl.findOne(userId);
		String workspaceUUID;
		if (workspaceList == null) {
			workspaceList = new BasicDBObject();
			workspaceList.put(UUID, userId);

			Workspace workspace = workspaceBuilder.getWorkspace();
			workspace.setCreator(userId);
			workspace.setWorkspaceName("Default Workspace");
			workspace.saveWorkspace();

			workspaceUUID = workspace.getWorkspaceUUID();
			workspaceList.put(ACTIVEWORKSPACE, workspaceUUID);
			Collection<String> workspaces = new HashSet<String>();
			workspaces.add(workspaceUUID);
			workspaceList
					.put(MongoWorkspaceConstants.WORKSPACELIST, workspaces);
			userWorkspacesColl.save(workspaceList);
		}
		workspaceUUID = (String) workspaceList.get(ACTIVEWORKSPACE);
		return getWorkspace(workspaceUUID);
	}

	@Override
	public Collection<Workspace> getUserWorkspaces(String userId)
			throws WorkspaceException {
		ArrayList<Workspace> workspaceList = new ArrayList<Workspace>();
		DBObject workspaceObject = userWorkspacesColl.findOne(userId);
		BasicDBList workspaces = (BasicDBList) workspaceObject
				.get(MongoWorkspaceConstants.WORKSPACELIST);
		Iterator<Object> workspacesIterator = workspaces.iterator();
		while (workspacesIterator.hasNext()) {
			Workspace workspace = workspaceBuilder
					.getWorkspace((String) workspacesIterator.next());
			workspaceList.add(workspace);
		}
		return workspaceList;
	}

	public DBCollection getuserWorkspacesColl() {
		return userWorkspacesColl;
	}

	public void setuserWorkspacesColl(DBCollection userWorkspacesColl) {
		this.userWorkspacesColl = userWorkspacesColl;
	}

	public AuditService getAuditService() {
		return auditService;
	}

	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}
}
