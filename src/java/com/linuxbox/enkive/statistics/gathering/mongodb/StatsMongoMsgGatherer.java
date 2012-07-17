package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.ARCHIVE_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class StatsMongoMsgGatherer extends AbstractGatherer {

	protected Mongo m = null;
	protected DBCollection messageColl;
	protected DB messageDb;

	public StatsMongoMsgGatherer(Mongo m, String dbName, String collName,
			String serviceName, String schedule) {
		super(serviceName, schedule);
		this.m = m;
		messageDb = m.getDB(dbName);
		messageColl = messageDb.getCollection(collName);
	}

	public StatsMongoMsgGatherer(Mongo m, String dbName, String collName,
			String serviceName, String schedule, List<String> keys) throws GathererException {
		super(serviceName, schedule, keys);
		this.m = m;
		messageDb = m.getDB(dbName);
		messageColl = messageDb.getCollection(collName);
	}
	
	@Override
	public Map<String, Object> getStatistics() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(ARCHIVE_SIZE, messageColl.count());
		result.put(STAT_TIME_STAMP, new Date());
		return result;
	}
}
