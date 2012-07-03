package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INDEX_SIZES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_LAST_EXTENT_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_INDEX;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_OBJS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE_COLL;
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
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.KeyDef;
import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsMongoCollectionGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering");

	public static void main(String args[]) throws UnknownHostException,
			MongoException {
		StatsMongoCollectionGatherer collProps = new StatsMongoCollectionGatherer(
				new Mongo(), "enkive", "collService", "cronExpression");
		System.out.println(collProps.getStatistics());
		String[] keys = { STAT_TYPE, STAT_NAME, STAT_DATA_SIZE, };
		System.out.println(collProps.getStatistics(keys));
	}

	protected DB db;

	protected Mongo m;

	public StatsMongoCollectionGatherer(Mongo m, String dbName,
			String serviceName, String schedule) {
		super(serviceName, schedule);
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
		collStats.put(STAT_TIME_STAMP, System.currentTimeMillis());
		return collStats;
	}

	// overwrites the abstract implementation b/c collections are stored
	// embedded
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

	private Map<String, Object> getStats(String collectionName) {
		if (db.collectionExists(collectionName)) {
			Map<String, Object> stats = createMap();
			Map<String, Object> temp = db.getCollection(collectionName)
					.getStats();
 			stats.put(STAT_TYPE, STAT_TYPE_COLL);
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

	@Override
	protected List<KeyDef> keyBuilder() {
		List<KeyDef> keys = new LinkedList<KeyDef>();
		keys.add(new KeyDef("*." + STAT_TYPE + ":"));
		keys.add(new KeyDef("*." + STAT_NS + ":"));
		keys.add(new KeyDef("*." + STAT_NUM_OBJS + ":" + GRAIN_AVG + ","
				+ GRAIN_MAX + "," + GRAIN_MIN));
		keys.add(new KeyDef("*." + STAT_AVG_OBJ_SIZE + ":" + GRAIN_AVG + ","
				+ GRAIN_MAX + "," + GRAIN_MIN));
		keys.add(new KeyDef("*." + STAT_DATA_SIZE + ":" + GRAIN_AVG + ","
				+ GRAIN_MAX + "," + GRAIN_MIN));
		keys.add(new KeyDef("*." + STAT_TOTAL_SIZE + ":" + GRAIN_AVG + ","
				+ GRAIN_MAX + "," + GRAIN_MIN));
		keys.add(new KeyDef("*." + STAT_NUM_EXTENT + ":" + GRAIN_AVG + ","
				+ GRAIN_MAX + "," + GRAIN_MIN));
		keys.add(new KeyDef("*." + STAT_LAST_EXTENT_SIZE + ":" + GRAIN_AVG
				+ "," + GRAIN_MAX + "," + GRAIN_MIN));
		keys.add(new KeyDef("*." + STAT_NUM_INDEX + ":" + GRAIN_AVG + ","
				+ GRAIN_MAX + "," + GRAIN_MIN));
		keys.add(new KeyDef("*." + STAT_TOTAL_INDEX_SIZE + ":" + GRAIN_AVG
				+ "," + GRAIN_MAX + "," + GRAIN_MIN));
		keys.add(new KeyDef("*." + STAT_INDEX_SIZES + ".*:" + GRAIN_AVG + ","
				+ GRAIN_MAX + "," + GRAIN_MIN));
		return keys;
	}
}
