package com.linuxbox.enkive.statistics.storage.mongodb;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.linuxbox.enkive.statistics.storage.StatsStorageException;
import com.linuxbox.enkive.statistics.storage.StatsStorageService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoStatsStorageService_unused implements StatsStorageService {
	/*
	 * NOAH: feel free to change the name or to put it in a special Mongo
	 * constants interface/class for the MongoStatsStorageService
	 */
	static final String STATS_COLLECTION = "stats";

	private DBCollection statsCollection;

	/* NOAH: you'll need to fill this out */

	public MongoStatsStorageService_unused(Mongo mongo, String dbName) {
		final DB mongoDB = mongo.getDB(dbName);

		statsCollection = mongoDB.getCollection(STATS_COLLECTION);
	}

	@Override
	public void storeStatistics(String service, Date timestamp,
			Map<String, Object> data) throws StatsStorageException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<Object> queryStatistics(String statName,
			Date startingTimestamp, Date endingTimestamp)
			throws StatsStorageException {
		// TODO Auto-generated method stub
		try {
			// do something with the Mongo back-end
		} catch (MongoException e) {
			// here we've caught one kind of exception and turned it into
			// another; we could have also done ''new
			// StatsStorageException("some message", e)''
			throw new StatsStorageException(e);
		}
		return null; // auto-generated
	}

}
