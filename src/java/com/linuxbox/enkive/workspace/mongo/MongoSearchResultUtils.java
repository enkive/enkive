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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.linuxbox.enkive.archiver.MesssageAttributeConstants;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

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

	public LinkedHashSet<String> sortMessagesByDate(Set<String> messageIds,
			int sortDir) {
		return sortMessages(messageIds, MesssageAttributeConstants.DATE,
				sortDir);
	}

	public LinkedHashSet<String> sortMessagesBySender(Set<String> messageIds,
			int sortDir) {
		return sortMessages(messageIds, MesssageAttributeConstants.FROM,
				sortDir);
	}

	public LinkedHashSet<String> sortMessagesByReceiver(Set<String> messageIds,
			int sortDir) {
		return sortMessages(messageIds, MesssageAttributeConstants.TO, sortDir);
	}

	public LinkedHashSet<String> sortMessagesBySubject(Set<String> messageIds,
			int sortDir) {
		return sortMessages(messageIds, MesssageAttributeConstants.SUBJECT,
				sortDir);
	}

	protected LinkedHashSet<String> sortMessages(Set<String> messageIds,
			String sortField, int sortDirection) {
		LinkedHashSet<String> sortedIds = new LinkedHashSet<String>();
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
		results = results.sort(orderBy);
		for (DBObject result : results.toArray())
			sortedIds.add((String) result.get("_id"));
		return sortedIds;
	}

	public List<String> sortSearchResults(Collection<String> searchResultIds,
			String sortField, int sortDirection) {
		ArrayList<String> sortedIds = new ArrayList<String>();
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
		results = results.sort(orderBy);
		for (DBObject result : results.toArray())
			sortedIds.add(((ObjectId) result.get("_id")).toString());

		return sortedIds;
	}

}
