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

import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHCRITERIA;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHNAME;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoSearchQueryBuilder implements SearchQueryBuilder {

	protected Mongo m;
	protected DB searchQueryDB;
	protected DBCollection searchQueryColl;

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	public MongoSearchQueryBuilder(Mongo m, String searchQueryDBName,
			String searchQueryCollName) {
		this.m = m;
		searchQueryDB = m.getDB(searchQueryDBName);
		searchQueryColl = searchQueryDB.getCollection(searchQueryCollName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SearchQuery getSearchQuery(String searchQueryId)
			throws WorkspaceException {
		SearchQuery query = new MongoSearchQuery(m, searchQueryDB.getName(),
				searchQueryColl.getName());
		DBObject queryObject = searchQueryColl.findOne(ObjectId
				.massageToObjectId(searchQueryId));

		query.setId(searchQueryId);
		query.setName((String) queryObject.get(SEARCHNAME));
		query.setCriteria(((BasicDBObject) queryObject.get(SEARCHCRITERIA))
				.toMap());

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Retrieved Search Query " + query.getName() + " - "
					+ query.getId());
		return query;
	}

	@Override
	public SearchQuery getSearchQuery() throws WorkspaceException {
		return new MongoSearchQuery(m, searchQueryDB.getName(),
				searchQueryColl.getName());
	}

}
