package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.ARCHIVE_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.util.HashMap;
import java.util.Map;

import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
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

	@Override
	public Map<String, Object> getStatistics() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(STAT_TIME_STAMP, System.currentTimeMillis());
		result.put(ARCHIVE_SIZE, messageColl.count());
		return result;
	}
/*
	@Override
	protected List<KeyDef> keyBuilder() {
		List<KeyDef> keys = new LinkedList<KeyDef>();
		keys.add(new KeyDef(ARCHIVE_SIZE + ":" + GRAIN_AVG + "," + GRAIN_MAX
				+ "," + GRAIN_MIN));
		return keys;
	}
*/
}
