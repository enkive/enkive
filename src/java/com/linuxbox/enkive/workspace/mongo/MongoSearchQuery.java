/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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

import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHCRITERIA;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHNAME;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoSearchQuery extends SearchQuery {

	protected Mongo m;
	protected DB searchQueryDB;
	protected DBCollection searchQueryColl;

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	public MongoSearchQuery(Mongo m, String searchQueryDBName,
			String searchQueryCollName) {
		this.m = m;
		searchQueryDB = m.getDB(searchQueryDBName);
		searchQueryColl = searchQueryDB.getCollection(searchQueryCollName);
	}

	@Override
	public void saveSearchQuery() throws WorkspaceException {
		BasicDBObject searchQueryObject = new BasicDBObject();
		searchQueryObject.put(SEARCHNAME, getName());
		searchQueryObject.put(SEARCHCRITERIA, getCriteria());

		if (getId() != null && !getId().isEmpty()) {
			DBObject toUpdate = searchQueryColl.findOne(ObjectId
					.massageToObjectId(getId()));
			if (toUpdate != null) {
				searchQueryColl.update(toUpdate, searchQueryObject);
				searchQueryObject.put(UUID, toUpdate.get(UUID));
			}
		}
		if (searchQueryObject.getString(UUID) == null) {
			searchQueryColl.insert(searchQueryObject);
			setId(searchQueryObject.getString(UUID));
		}

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Saved Search Query - "
					+ searchQueryObject.getString(UUID));

	}

	@Override
	public void deleteSearchQuery() throws WorkspaceException {
		DBObject searchQueryObject = searchQueryColl.findOne(ObjectId
				.massageToObjectId(getId()));
		searchQueryColl.remove(searchQueryObject);
	}

}
