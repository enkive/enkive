package com.linuxbox.enkive.imap.mongo;

import java.util.Date;

import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;
import com.mongodb.BasicDBObject;
import com.mongodb.Mongo;

public class MongoImapAccountCreationMessageSearchService extends
		MongoMessageSearchService {

	//TODO This class should extend the functions provided in the search service
	//to get the applicable range of dates to create the folders.
	
	public MongoImapAccountCreationMessageSearchService(Mongo m, String dbName,
			String collName) {
		super(m, dbName, collName);
	}
	
	public Date getEarliestMessageDate(String username){
		//Search for the earliest date, how best to do that?
		BasicDBObject searchObject = new BasicDBObject();
		return null;
	}
	
	public Date getLatestMessageDate(String username){
		return null;
	}

}
