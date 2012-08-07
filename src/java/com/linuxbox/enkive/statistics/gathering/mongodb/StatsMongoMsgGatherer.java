package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.ARCHIVE_SIZE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linuxbox.enkive.statistics.RawStats;
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
	public RawStats getStatistics() {
		Map<String, Object> stats = new HashMap<String, Object>();
		stats.put(ARCHIVE_SIZE, messageColl.count());
		
		RawStats result = new RawStats();
		result.setStatsMap(stats);
		result.setTimestamp(new Date());
		return result;
	}
}
