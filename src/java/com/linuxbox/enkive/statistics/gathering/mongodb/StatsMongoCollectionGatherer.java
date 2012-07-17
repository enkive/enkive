package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INDEX_SIZES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_LAST_EXTENT_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_INDEX;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_OBJS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_COUNT;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_INDEX_SIZES;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_LAST_EXTENT_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_NS;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_NUM_INDEX;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_STORAGE_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_TOTAL_INDEX_SIZE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.mongodb.DB;
import com.mongodb.Mongo;

public class StatsMongoCollectionGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering");

	protected DB db;

	protected Mongo m;

	public StatsMongoCollectionGatherer(Mongo m, String dbName,
			String serviceName, String schedule) {
		super(serviceName, schedule);
		this.m = m;
		db = m.getDB(dbName);
	}
	
	public StatsMongoCollectionGatherer(Mongo m, String dbName,
			String serviceName, String schedule, List<String> keys) throws GathererException {
		super(serviceName, schedule, keys);
		this.m = m;
		db = m.getDB(dbName);
	}

	@Override
	public Map<String, Object> getStatistics() {
		Map<String, Object> collStats = new HashMap<String, Object>();
		for (String collName : db.getCollectionNames()) {
			String key = collName;
			if (collName.startsWith("$")) {
				collName = collName.replaceFirst("$", "-");
			}
			collName = collName.replace('.', '-');
			collStats.put(collName, getStats(key));
		}
		collStats.put(STAT_TIME_STAMP, new Date(System.currentTimeMillis()));
		return collStats;
	}
	
	@Override
	public Map<String, Object> getStatistics(String[] keys) {
		if (keys == null) {
			return getStatistics();
		}
		Map<String, Object> selectedStats = createMap();
		for (String collName : db.getCollectionNames()) {
			Map<String, Object> stats = getStats(collName);
			Map<String, Object> temp = createMap();
			for (String key : keys) {
				if (stats.get(key) != null) {
					temp.put(key, stats.get(key));
				}
			}
			selectedStats.put(collName, temp);
		}
		selectedStats.put(STAT_SERVICE_NAME, attributes.getName());
		selectedStats.put(STAT_TIME_STAMP, System.currentTimeMillis());

		return selectedStats;
	}

	/**
	 * gets the statistics cooresponding to a given collection
	 * @param collectionName - the name of the collection on which to gather stats
	 * @return the stats collected
	 */
	private Map<String, Object> getStats(String collectionName) {
		if (db.collectionExists(collectionName)) {
			Map<String, Object> stats = createMap();
			Map<String, Object> temp = db.getCollection(collectionName)
					.getStats();
			stats.put(STAT_NS, temp.get(MONGO_NS));
			stats.put(STAT_NUM_OBJS, temp.get(MONGO_COUNT));
			stats.put(STAT_AVG_OBJ_SIZE, temp.get(MONGO_AVG_OBJ_SIZE));
			stats.put(STAT_DATA_SIZE, temp.get(MONGO_SIZE));
			stats.put(STAT_TOTAL_SIZE, temp.get(MONGO_STORAGE_SIZE));
			stats.put(STAT_NUM_EXTENT, temp.get(MONGO_NUM_EXTENT));
			stats.put(STAT_LAST_EXTENT_SIZE, temp.get(MONGO_LAST_EXTENT_SIZE));
			stats.put(STAT_NUM_INDEX, temp.get(MONGO_NUM_INDEX));
			stats.put(STAT_TOTAL_INDEX_SIZE, temp.get(MONGO_TOTAL_INDEX_SIZE));
			stats.put(STAT_INDEX_SIZES, temp.get(MONGO_INDEX_SIZES));
			return stats;
		} else {
			LOGGER.warn("Collection " + collectionName + " does not exist");
			return null;
		}
	}
}
