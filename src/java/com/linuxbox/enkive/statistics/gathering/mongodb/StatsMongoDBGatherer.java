package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FILE_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_COLLECTIONS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_INDEX;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_OBJS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE_DB;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_DATA_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_FILE_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_INDEXES;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_NUM_COLLECTIONS;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_NUM_OBJS;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_STORAGE_SIZE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.KeyDef;
import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsMongoDBGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering");

	public static void main(String args[]) throws UnknownHostException,
			MongoException {
		StatsMongoDBGatherer dbProps = new StatsMongoDBGatherer(new Mongo(),
				"enkive", "hi", "*");
		System.out.println(dbProps.getStatistics());
		String[] keys = { STAT_TYPE, STAT_NAME, STAT_NUM_OBJS, STAT_FILE_SIZE };
		System.out.println(dbProps.getStatistics(keys));
	}

	protected DB db;

	protected Mongo m;

	public StatsMongoDBGatherer(Mongo m, String dbName, String serviceName,
			String schedule) {
		super(serviceName, schedule);
		this.m = m;
		db = m.getDB(dbName);
	}

	@Override
	public Map<String, Object> getStatistics() {
		return getStats();
	}

	public BasicDBObject getStats() {
		BasicDBObject stats = new BasicDBObject();
		BasicDBObject temp = db.getStats();
		stats.put(STAT_TYPE, STAT_TYPE_DB);
		stats.put(STAT_NAME, db.getName());
		stats.put(STAT_NUM_COLLECTIONS, temp.get(MONGO_NUM_COLLECTIONS));
		stats.put(STAT_NUM_OBJS, temp.get(MONGO_NUM_OBJS));
		stats.put(STAT_AVG_OBJ_SIZE, temp.get(MONGO_AVG_OBJ_SIZE));
		stats.put(STAT_DATA_SIZE, temp.get(MONGO_DATA_SIZE));
		stats.put(STAT_TOTAL_SIZE, temp.get(MONGO_STORAGE_SIZE));
		stats.put(STAT_NUM_INDEX, temp.get(MONGO_INDEXES));
		stats.put(STAT_TOTAL_INDEX_SIZE, temp.get(MONGO_INDEX_SIZE));
		stats.put(STAT_NUM_EXTENT, temp.get(MONGO_NUM_EXTENT));
		stats.put(STAT_FILE_SIZE, temp.get(MONGO_FILE_SIZE));
		stats.put(STAT_TIME_STAMP, System.currentTimeMillis());
		return stats;
	}

	@Override
	protected List<KeyDef> keyBuilder() {
		List<KeyDef> keys = new LinkedList<KeyDef>();
		keys.add(new KeyDef(STAT_TYPE + ":"));
		keys.add(new KeyDef(STAT_NAME + ":"));
		keys.add(new KeyDef(STAT_NUM_COLLECTIONS + ":" + GRAIN_AVG + ","
				+ GRAIN_MAX + "," + GRAIN_MIN));
		keys.add(new KeyDef(STAT_NUM_OBJS + ":" + GRAIN_AVG + "," + GRAIN_MAX
				+ "," + GRAIN_MIN));
		keys.add(new KeyDef(STAT_AVG_OBJ_SIZE + ":" + GRAIN_AVG + ","
				+ GRAIN_MAX + "," + GRAIN_MIN));
		keys.add(new KeyDef(STAT_DATA_SIZE + ":" + GRAIN_AVG + "," + GRAIN_MAX
				+ "," + GRAIN_MIN));
		keys.add(new KeyDef(STAT_TOTAL_SIZE + ":" + GRAIN_AVG + "," + GRAIN_MAX
				+ "," + GRAIN_MIN));
		keys.add(new KeyDef(STAT_NUM_INDEX + ":" + GRAIN_AVG + "," + GRAIN_MAX
				+ "," + GRAIN_MIN));
		keys.add(new KeyDef(STAT_TOTAL_INDEX_SIZE + ":" + GRAIN_AVG + ","
				+ GRAIN_MAX + "," + GRAIN_MIN));
		keys.add(new KeyDef(STAT_NUM_EXTENT + ":" + GRAIN_AVG + "," + GRAIN_MAX
				+ "," + GRAIN_MIN));
		keys.add(new KeyDef(STAT_FILE_SIZE + ":" + GRAIN_AVG + "," + GRAIN_MAX
				+ "," + GRAIN_MIN));
		return keys;
	}
}
