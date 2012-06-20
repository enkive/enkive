package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.ARCHIVE_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_ATTACH;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
public class StatsMongoMsgGatherer extends AbstractGatherer {

	protected Mongo m = null;
	protected DB messageDb;
	protected DBCollection messageColl;
	
	public StatsMongoMsgGatherer(Mongo m, String dbName, String collName, String serviceName, String schedule) {
		super(serviceName, schedule);
		this.m = m;
		messageDb = m.getDB(dbName);
		messageColl = messageDb.getCollection(collName);
	}
	
	protected Map<String, Set<String>> keyBuilder(){
		Map<String, Set<String>> keys = new HashMap<String, Set<String>>();
		keys.put(STAT_SERVICE_NAME, null);
		keys.put(ARCHIVE_SIZE, makeCreator(GRAIN_AVG));
		return keys;
	}

	@Override
	public Map<String, Object> getStatistics() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(STAT_TIME_STAMP, System.currentTimeMillis());
		result.put(ARCHIVE_SIZE, messageColl.count());
		return result;
	}

	public static void main(String args[]) throws UnknownHostException,
			MongoException {
		StatsMongoAttachmentsGatherer attachProps = new StatsMongoAttachmentsGatherer(
				new Mongo(), "enkive", "fs", "name", "cron");
		System.out.println(attachProps.getStatistics());
		String[] keys = { STAT_TYPE, STAT_NAME, STAT_DATA_SIZE, STAT_AVG_ATTACH };
		System.out.println(attachProps.getStatistics(keys));
	}
}
