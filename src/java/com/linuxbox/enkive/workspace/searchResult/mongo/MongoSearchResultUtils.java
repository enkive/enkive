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
package com.linuxbox.enkive.workspace.searchResult.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bson.types.ObjectId;

import com.linuxbox.enkive.archiver.MesssageAttributeConstants;
import com.linuxbox.enkive.workspace.searchResult.ResultPageException;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

/**
 * Utility class to sort SearchResults by characteristics of the messages in the
 * results.
 * @author dang
 *
 */
public class MongoSearchResultUtils {

	DBCollection messageColl;
	DBCollection searchResultColl;

	public MongoSearchResultUtils(MongoClient m, String messageDB,
			String messageCollName, String searchResultCollName) {
		DB messageDb = m.getDB(messageDB);
		this.messageColl = messageDb.getCollection(messageCollName);
		this.searchResultColl = messageDb.getCollection(searchResultCollName);
	}

	public MongoSearchResultUtils(MongoDbInfo messageInfo,
			MongoDbInfo searchResultInfo) {
		this.messageColl = messageInfo.getCollection();
		this.searchResultColl = searchResultInfo.getCollection();
	}

	public List<String> sortMessagesByDate(Collection<String> messageIds,
			int sortDir, int pageNum, int pageSize)
			throws ResultPageException {
		return sortMessages(messageIds, MesssageAttributeConstants.DATE,
				sortDir, pageNum, pageSize);
	}

	public List<String> sortMessagesBySender(Collection<String> messageIds,
			int sortDir, int pageNum, int pageSize)
			throws ResultPageException {
		return sortMessages(messageIds, MesssageAttributeConstants.FROM,
				sortDir, pageNum, pageSize);
	}

	public List<String> sortMessagesByReceiver(Collection<String> messageIds,
			int sortDir, int pageNum, int pageSize)
			throws ResultPageException {
		return sortMessages(messageIds, MesssageAttributeConstants.TO, sortDir,
				pageNum, pageSize);
	}

	public List<String> sortMessagesBySubject(Collection<String> messageIds,
			int sortDir, int pageNum, int pageSize)
			throws ResultPageException {
		return sortMessages(messageIds, MesssageAttributeConstants.SUBJECT,
				sortDir, pageNum, pageSize);
	}

	protected List<String> sortMessages(Collection<String> messageIds,
			String sortField, int sortDirection, int pageNum, int pageSize)
			throws ResultPageException {
		ArrayList<String> sortedIds = new ArrayList<String>();
		// Only want to return the ids
		BasicDBObject keys = new BasicDBObject();
		keys.put("_id", 1);
		keys.put(sortField, 1);

		BasicDBObject query = new BasicDBObject();
		// Build object with IDs
		BasicDBList idList = new BasicDBList();
		idList.addAll(messageIds);
		BasicDBObject idQuery = new BasicDBObject();
		idQuery.put("$in", idList);
		query.put("_id", idQuery);
		// Add sort query

		DBCursor results = messageColl.find(query, keys);
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put(sortField, sortDirection);
		try {
		results = results.sort(orderBy).skip((pageNum - 1) * pageSize).limit(pageSize);
		for (DBObject result : results)
			sortedIds.add((String)result.get("_id"));
		} catch(MongoException e) {
			// Mongo failed to get the page.  Create an error message for the user.
			throw new ResultPageException("MongoDB failed to get the requested page of results", e);
		}
		return sortedIds;
	}

	public List<String> sortSearchResults(Collection<String> searchResultIds,
			String sortField, int sortDirection, int pageNum, int pageSize) {
		ArrayList<String> sortedIds = new ArrayList<String>(pageSize);
		// Only want to return the ids
		BasicDBObject keys = new BasicDBObject();
		keys.put("_id", 1);
		keys.put(sortField, 1);

		BasicDBObject query = new BasicDBObject();
		// Build object with IDs
		BasicDBList idList = new BasicDBList();
		for (String Id : searchResultIds)
			idList.add(ObjectId.massageToObjectId(Id));

		BasicDBObject idQuery = new BasicDBObject();
		idQuery.put("$in", idList);
		query.put("_id", idQuery);
		// Add sort query

		DBCursor results = searchResultColl.find(query, keys);
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put(sortField, sortDirection);
		results = results.sort(orderBy).skip((pageNum - 1) * pageSize).limit(pageSize);
		for (DBObject result : results.toArray())
			sortedIds.add((String)result.get("_id"));

		return sortedIds;
	}

}
