package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.ARCHIVE_SIZE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linuxbox.enkive.statistics.InstantRawStats;
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
			String serviceName, String humanName, String schedule) {
		super(serviceName, humanName, schedule);
		this.m = m;
		messageDb = m.getDB(dbName);
		messageColl = messageDb.getCollection(collName);
	}

	public StatsMongoMsgGatherer(Mongo m, String dbName, String collName,
			String serviceName, String humanName, String schedule, List<String> keys) throws GathererException {
		super(serviceName, humanName, schedule, keys);
		this.m = m;
		messageDb = m.getDB(dbName);
		messageColl = messageDb.getCollection(collName);
	}
	
	@Override
	public RawStats getStatistics() {
		Map<String, Object> stats = new HashMap<String, Object>();
		stats.put(ARCHIVE_SIZE, messageColl.count());
		
		RawStats result = new InstantRawStats(stats, new Date());
		return result;
	}
}
