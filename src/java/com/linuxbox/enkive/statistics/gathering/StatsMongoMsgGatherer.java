package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.ARCHIVE_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.*;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsMongoMsgGatherer extends AbstractGatherer {

	protected Mongo m = null;
	protected DB messageDb;
	protected DBCollection messageColl;

	public StatsMongoMsgGatherer(Mongo m, String dbName, String collName) {
		this.m = m;
		messageDb = m.getDB(dbName);
		messageColl = messageDb.getCollection(collName);
		setAttributes();
	}

	@Override
	public Map<String, Object> getStatistics() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(STAT_TIME_STAMP, System.currentTimeMillis());
		result.put(ARCHIVE_SIZE, messageColl.count());
		attributes.incrementTime();
		return result;
	}
	
	public static void main(String args[]) throws UnknownHostException, MongoException{
		StatsMongoAttachmentsGatherer attachProps = new StatsMongoAttachmentsGatherer(new Mongo(), "enkive", "fs");
		System.out.println(attachProps.getStatistics());
		String[] keys = {STAT_TYPE, STAT_NAME, STAT_DATA_SIZE, STAT_AVG_ATTACH };
		System.out.println(attachProps.getStatistics(keys));
	}
}
