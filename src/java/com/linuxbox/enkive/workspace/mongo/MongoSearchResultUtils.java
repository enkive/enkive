package com.linuxbox.enkive.workspace.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.linuxbox.enkive.archiver.MesssageAttributeConstants;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoSearchResultUtils {

	Mongo m;
	DB messageDb;
	DBCollection messageColl;
	DBCollection searchResultColl;

	public MongoSearchResultUtils(Mongo m, String messageDB,
			String messageColl, String searchResultColl) {
		this.m = m;
		this.messageDb = m.getDB(messageDB);
		this.messageColl = messageDb.getCollection(messageColl);
		this.searchResultColl = messageDb.getCollection(searchResultColl);
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

	public List<String> sortSearchResults(
			Collection<String> searchResultIds, String sortField,
			int sortDirection) {
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
