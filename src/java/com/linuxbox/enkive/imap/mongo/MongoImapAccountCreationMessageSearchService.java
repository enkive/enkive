package com.linuxbox.enkive.imap.mongo;

import java.util.Calendar;
import java.util.Date;

import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;
import com.mongodb.BasicDBObject;
import com.mongodb.Mongo;

public class MongoImapAccountCreationMessageSearchService extends
		MongoMessageSearchService {

	// TODO This class should extend the functions provided in the search
	// service
	// to get the applicable range of dates to create the folders.

	public MongoImapAccountCreationMessageSearchService(Mongo m, String dbName,
			String collName) {
		super(m, dbName, collName);
	}

	public Date getEarliestMessageDate(String username) {
		// Search for the earliest date, how best to do that?
		BasicDBObject searchObject = new BasicDBObject();
		Calendar startingDate = Calendar.getInstance();
		startingDate.set(Calendar.YEAR, 1999);
		startingDate.set(Calendar.DAY_OF_YEAR, 1);
		
		return startingDate.getTime();
	}

	public Date getLatestMessageDate(String username) {
		return new Date();
	}

}
