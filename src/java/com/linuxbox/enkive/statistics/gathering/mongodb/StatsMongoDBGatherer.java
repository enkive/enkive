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

import com.linuxbox.enkive.statistics.VarsMaker;
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

	public StatsMongoDBGatherer(Mongo m, String dbName, String serviceName,
			String humanName, List<String> keys) throws GathererException {
		super(serviceName, humanName, keys);
		this.m = m;
		db = m.getDB(dbName);
	}

	@Override
	protected Map<String, Object> getPointStatistics(Date startTimestamp,
			Date endTimestamp) throws GathererException {
		Map<String, Object> pointStats = VarsMaker.createMap();
		BasicDBObject temp = db.getStats();
		pointStats.put(STAT_NAME, db.getName());
		pointStats.put(STAT_NUM_COLLECTIONS, temp.get(MONGO_NUM_COLLECTIONS));
		pointStats.put(STAT_NUM_OBJS, temp.get(MONGO_NUM_OBJS));
		pointStats.put(STAT_AVG_OBJ_SIZE, temp.get(MONGO_AVG_OBJ_SIZE));
		pointStats.put(STAT_DATA_SIZE, temp.get(MONGO_DATA_SIZE));
		pointStats.put(STAT_TOTAL_SIZE, temp.get(MONGO_STORAGE_SIZE));
		pointStats.put(STAT_NUM_INDEX, temp.get(MONGO_INDEXES));
		pointStats.put(STAT_TOTAL_INDEX_SIZE, temp.get(MONGO_INDEX_SIZE));
		pointStats.put(STAT_NUM_EXTENT, temp.get(MONGO_NUM_EXTENT));
		pointStats.put(STAT_FILE_SIZE, temp.get(MONGO_FILE_SIZE));

		return pointStats;
	}

	@Override
	protected Map<String, Object> getIntervalStatistics(Date startTimestamp,
			Date endTimestamp) throws GathererException {
		return null;
	}
}
