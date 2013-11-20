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
package com.linuxbox.enkive.workspace.searchResult.mongo;

import static com.linuxbox.enkive.web.WebConstants.SORTBYDATE;
import static com.linuxbox.enkive.web.WebConstants.SORTBYRECEIVER;
import static com.linuxbox.enkive.web.WebConstants.SORTBYSENDER;
import static com.linuxbox.enkive.web.WebConstants.SORTBYSUBJECT;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHQUERYID;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHRESULTS;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.UUID;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Implementation of @ref SearchResult stored in MongoDB.  Results are stored in
 * a collection named "searchResults", and consist of a document per query
 * containing the list of message IDs matching that query at the time the query
 * was run, and the ID of the query in question.
 * @author dang
 *
 */
public class MongoSearchResult extends SearchResult {
	DBCollection searchResultsColl;
	protected MongoSearchResultUtils searchResultUtils;

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	public MongoSearchResult(DBCollection searchResultsColl) {
		this.searchResultsColl = searchResultsColl;
	}

	@Override
	public void saveSearchResult() throws WorkspaceException {

		BasicDBObject searchResultObject = new BasicDBObject();
		searchResultObject.put(SEARCHRESULTS, BasicDBObjectBuilder.start(getMessageIds()).get());
		searchResultObject.put(SEARCHQUERYID, getSearchQueryId());

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

	/*
	 * (non-Javadoc)
	 * @see com.linuxbox.enkive.workspace.searchResult.SearchResult#getPage(java.lang.String, int, int, int)
	 */
	public List<String> getPage(String sortBy, int sortDir, int pageNum, int pageSize)
			throws WorkspaceException {
		if (sortBy == null || sortBy.equals("")) {
			// No sorting, just paginate
			List<String> page = new ArrayList<String>(pageSize);
			int offset = 0;
			for	(String id : getMessageIds().values()) {
				if (offset < (pageNum - 1) * pageSize) {
					offset++;
					continue;
				}
				page.add(id);
				if (page.size() == pageSize) {
					// Page full
					return page;
				}
			}

			// Ran out
			return (page);
		} else if (sortBy.equals(SORTBYDATE)) {
			return searchResultUtils.sortMessagesByDate(getMessageIds().values(),
					sortDir, pageNum, pageSize);
		} else if (sortBy.equals(SORTBYSUBJECT)) {
			return searchResultUtils.sortMessagesBySubject(getMessageIds().values(),
					sortDir, pageNum, pageSize);
		} else if (sortBy.equals(SORTBYSENDER)) {
			return searchResultUtils.sortMessagesBySender(getMessageIds().values(),
					sortDir, pageNum, pageSize);
		} else if (sortBy.equals(SORTBYRECEIVER)) {
			return searchResultUtils.sortMessagesByReceiver(getMessageIds().values(),
					sortDir, pageNum, pageSize);
		}

		throw new WorkspaceException("Unknown sort type " + sortBy);
	}

	public MongoSearchResultUtils getSearchResultUtils() {
		return searchResultUtils;
	}

	public void setSearchResultUtils(MongoSearchResultUtils searchResultUtils) {
		this.searchResultUtils = searchResultUtils;
	}

}
