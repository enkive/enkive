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

import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.EXECUTEDBY;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.EXECUTIONTIMESTAMP;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHISSAVED;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHQUERYID;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHRESULTS;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHSTATUS;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.UUID;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoSearchResult extends SearchResult {

	Mongo m;
	DB searchResultsDB;
	DBCollection searchResultsColl;
	protected MongoSearchResultUtils searchResultUtils;

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	public MongoSearchResult(Mongo m, String searchResultsDBName,
			String searchResultsCollName, SearchQueryBuilder queryBuilder) {
		this.m = m;
		searchResultsDB = m.getDB(searchResultsDBName);
		searchResultsColl = searchResultsDB
				.getCollection(searchResultsCollName);
		setSearchQueryBuilder(queryBuilder);
	}

	@Override
	public void saveSearchResult() throws WorkspaceException {

		BasicDBObject searchResultObject = new BasicDBObject();
		searchResultObject.put(EXECUTIONTIMESTAMP, getTimestamp());
		searchResultObject.put(EXECUTEDBY, getExecutedBy());
		searchResultObject.put(SEARCHRESULTS, getMessageIds());
		searchResultObject.put(SEARCHSTATUS, getStatus().toString());
		searchResultObject.put(SEARCHQUERYID, getSearchQueryId());
		searchResultObject.put(SEARCHISSAVED, isSaved());

		if (getId() != null && !getId().isEmpty()) {
			DBObject toUpdate = searchResultsColl.findOne(ObjectId
					.massageToObjectId(getId()));
			if (toUpdate != null) {
				searchResultsColl.update(toUpdate, searchResultObject);
				searchResultObject.put(UUID, toUpdate.get(UUID));
			}
		}
		if (searchResultObject.getString(UUID) == null) {
			searchResultsColl.insert(searchResultObject);
			setId(searchResultObject.getString(UUID));
		}

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Saved Search Results - " + getId());

	}

	@Override
	public void deleteSearchResult() throws WorkspaceException {
		DBObject searchResultObject = searchResultsColl.findOne(ObjectId
				.massageToObjectId(getId()));
		searchResultsColl.remove(searchResultObject);
	}

	public void sortSearchResultMessages(String sortBy, int sortDir)
			throws WorkspaceException {
		Set<String> messageIds = getMessageIds();
		if (sortBy.equals(SORTBYDATE))
			setMessageIds(searchResultUtils.sortMessagesByDate(messageIds,
					sortDir));
		if (sortBy.equals(SORTBYSUBJECT))
			setMessageIds(searchResultUtils.sortMessagesBySubject(messageIds,
					sortDir));
		if (sortBy.equals(SORTBYSENDER))
			setMessageIds(searchResultUtils.sortMessagesBySender(messageIds,
					sortDir));
		if (sortBy.equals(SORTBYRECEIVER))
			setMessageIds(searchResultUtils.sortMessagesByReceiver(messageIds,
					sortDir));
	}

	public MongoSearchResultUtils getSearchResultUtils() {
		return searchResultUtils;
	}

	public void setSearchResultUtils(MongoSearchResultUtils searchResultUtils) {
		this.searchResultUtils = searchResultUtils;
	}

}
