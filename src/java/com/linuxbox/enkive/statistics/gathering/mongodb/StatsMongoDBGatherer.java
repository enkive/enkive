package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FILE_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_COLLECTIONS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_INDEX;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_OBJS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_DATA_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_FILE_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_INDEXES;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_NUM_COLLECTIONS;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_NUM_OBJS;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_STORAGE_SIZE;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;

public class StatsMongoDBGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering");

	protected DB db;

	protected Mongo m;

	public StatsMongoDBGatherer(Mongo m, String dbName, String serviceName, String humanName,
			String schedule) throws GathererException {
		super(serviceName, humanName, schedule);
		this.m = m;
		db = m.getDB(dbName);
	}
	
	public StatsMongoDBGatherer(Mongo m, String dbName, String serviceName, String humanName,
			String schedule, List<String> keys) throws GathererException {
		super(serviceName, humanName, schedule, keys);
		this.m = m;
		db = m.getDB(dbName);
	}

	@Override
	public RawStats getStatistics() {
		Map<String, Object> stats = createMap();
		BasicDBObject temp = db.getStats();
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
		
		RawStats result = new RawStats();
		result.setStatsMap(stats);
		result.setTimestamp(new Date());
		return result;
	}
}
