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
package com.linuxbox.enkive.workspace.searchQuery.mongo;

import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.EXECUTEDBY;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.EXECUTIONTIMESTAMP;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.LASTMONOTONICID;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHCRITERIA;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHISSAVED;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHNAME;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHRESULTID;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHSTATUS;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.UUID;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;
import com.linuxbox.enkive.workspace.searchResult.SearchResultBuilder;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * An implementation of the @ref SearchQueryBuilder factory for MongoDB.
 * Creates or finds MongoSearchQuery objects.
 * @author dang
 *
 */
public class MongoSearchQueryBuilder implements SearchQueryBuilder {
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	private DBCollection searchQueryColl;
	private SearchResultBuilder searchResultBuilder;

	public MongoSearchQueryBuilder(MongoClient m, String searchQueryDBName,
			String searchQueryCollName) {
		this(m.getDB(searchQueryDBName).getCollection(searchQueryCollName));
	}

	public MongoSearchQueryBuilder(MongoDbInfo dbInfo) {
		this(dbInfo.getCollection());
	}

	public MongoSearchQueryBuilder(DBCollection searchQueryColl) {
		this.searchQueryColl = searchQueryColl;
	}

	public SearchResultBuilder getSearchResultBuilder() {
		return searchResultBuilder;
	}

	public void setSearchResultBuilder(SearchResultBuilder searchResultBuilder) {
		this.searchResultBuilder = searchResultBuilder;
	}

	@Override
	public SearchQuery getSearchQuery() throws WorkspaceException {
		SearchQuery query = new MongoSearchQuery(searchQueryColl);
		SearchResult result = searchResultBuilder.getSearchResult();

		query.setId(new ObjectId().toString());
		result.setId(new ObjectId().toString());

		result.setSearchQueryId(query.getId());
		query.setResult(result);

		return query;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SearchQuery getSearchQuery(String searchQueryId)
			throws WorkspaceException {
		SearchQuery query = new MongoSearchQuery(searchQueryColl);
		DBObject queryObject = searchQueryColl.findOne(ObjectId
				.massageToObjectId(searchQueryId));

		query.setId(searchQueryId);
		query.setName((String) queryObject.get(SEARCHNAME));
		query.setResult(searchResultBuilder
				.getSearchResult((String) queryObject.get(SEARCHRESULTID)));
		query.setCriteria(((BasicDBObject) queryObject.get(SEARCHCRITERIA))
				.toMap());
		query.setTimestamp((Date) queryObject.get(EXECUTIONTIMESTAMP));
		query.setExecutedBy((String) queryObject.get(EXECUTEDBY));
		query.setStatus(SearchQuery.Status.valueOf((String) queryObject
				.get(SEARCHSTATUS)));
		query.setLastMonotonic((String) queryObject.get(LASTMONOTONICID));
		if (queryObject.get(SEARCHISSAVED) != null)
			query.setSaved((Boolean) queryObject.get(SEARCHISSAVED));
		if (LOGGER.isInfoEnabled())
			LOGGER.info("Retrieved Search Query " + query.getName() + " - "
					+ query.getId());
		return query;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<SearchQuery> getSearchQueries(
			Collection<String> searchQueryUUIDs) throws WorkspaceException {

		Collection<SearchQuery> queries = new HashSet<SearchQuery>();

		BasicDBObject dbQuery = new BasicDBObject();
		BasicDBList idList = new BasicDBList();
		for (String searchQueryUUID : searchQueryUUIDs)
			idList.add(ObjectId.massageToObjectId(searchQueryUUID));
		dbQuery.put("$in", idList);
		DBCursor searchQuery = searchQueryColl.find(new BasicDBObject(UUID,
				dbQuery));
		while (searchQuery.hasNext()) {
			MongoSearchQuery query = new MongoSearchQuery(searchQueryColl);
			DBObject queryObject = searchQuery.next();

			query.setId(((ObjectId) queryObject.get(UUID)).toString());
			query.setName((String) queryObject.get(SEARCHNAME));
			query.setResult(searchResultBuilder
					.getSearchResult((String) queryObject.get(SEARCHRESULTID)));
			query.setCriteria(((BasicDBObject) queryObject.get(SEARCHCRITERIA))
					.toMap());
			query.setTimestamp((Date) queryObject.get(EXECUTIONTIMESTAMP));
			query.setExecutedBy((String) queryObject.get(EXECUTEDBY));
			query.setLastMonotonic((String) queryObject.get(LASTMONOTONICID));
			query.setStatus(SearchQuery.Status.valueOf((String) queryObject
					.get(SEARCHSTATUS)));
			if (queryObject.get(SEARCHISSAVED) != null)
				query.setSaved((Boolean) queryObject.get(SEARCHISSAVED));
			queries.add(query);

		}
		return queries;
	}
}
