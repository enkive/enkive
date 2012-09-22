package com.linuxbox.enkive.imap.mongo;

import java.util.Date;

import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoImapAccountCreationMessageSearchService extends
		MongoMessageSearchService {

	public MongoImapAccountCreationMessageSearchService(Mongo m, String dbName,
			String collName) {
		super(m, dbName, collName);
	}

	public Date getEarliestMessageDate(String username) {
		BasicDBObject searchObject = new BasicDBObject();
		BasicDBObject fieldLimitObject = new BasicDBObject();
		fieldLimitObject.put("date", 1);
		DBCursor cursor = messageColl.find(searchObject, fieldLimitObject)
				.sort(fieldLimitObject).limit(1);
		DBObject earliestDateObject = cursor.next();
		Date earliestDate = (Date) earliestDateObject.get("date");
		return earliestDate;
	}

	public Date getLatestMessageDate(String username) {
		BasicDBObject searchObject = new BasicDBObject();
		BasicDBObject fieldLimitObject = new BasicDBObject();
		fieldLimitObject.put("date", 1);
		DBCursor cursor = messageColl.find(searchObject, fieldLimitObject)
				.sort(new BasicDBObject("date", -1)).limit(1);
		DBObject latestDateObject = cursor.next();
		Date latestDate = (Date) latestDateObject.get("date");
		return latestDate;
	}

}
