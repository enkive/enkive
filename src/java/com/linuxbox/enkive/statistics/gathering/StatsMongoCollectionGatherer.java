package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INDEX_SIZES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_LAST_EXTENT_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_INDEX;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_OBJS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ERROR;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsMongoCollectionGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering");

	protected Mongo m;
	protected DB db;

	public StatsMongoCollectionGatherer(Mongo m, String dbName, String serviceName, String schedule) {
		this.m = m;
		db = m.getDB(dbName);
		Map<String, String> keys = new HashMap<String, String>();
		keys.put(STAT_TIME_STAMP, "AVG");
		keys.put(STAT_NAME, null);
		keys.put(STAT_TYPE, null);
		keys.put(STAT_NS, null);
		keys.put(STAT_NUM_OBJS, "AVG");
		keys.put(STAT_AVG_OBJ_SIZE, "AVG");
		keys.put(STAT_DATA_SIZE, "AVG");
		keys.put(STAT_TOTAL_SIZE, "AVG");
		keys.put(STAT_NUM_EXTENT, "AVG");
		keys.put(STAT_LAST_EXTENT_SIZE, "AVG");
		keys.put(STAT_NUM_INDEX, "AVG");
		keys.put(STAT_TOTAL_INDEX_SIZE, "AVG");
		keys.put(STAT_INDEX_SIZES, "AVG");
		attributes = new GathererAttributes(serviceName, schedule, keys);
	}

	private Map<String, Object> getStats(String collectionName) {
		if (db.collectionExists(collectionName)) {
			Map<String, Object> stats = createMap();
			Map<String, Object> temp = db.getCollection(collectionName)
					.getStats();
			stats.put(STAT_TYPE, "collection");
			stats.put(STAT_NAME, collectionName);
			stats.put(STAT_NS, temp.get("ns"));
			stats.put(STAT_NUM_OBJS, temp.get("count"));
			stats.put(STAT_AVG_OBJ_SIZE, temp.get("avgObjSize"));
			stats.put(STAT_DATA_SIZE, temp.get("size"));
			stats.put(STAT_TOTAL_SIZE, temp.get("storageSize"));
			stats.put(STAT_NUM_EXTENT, temp.get("numExtents"));
			stats.put(STAT_LAST_EXTENT_SIZE, temp.get("lastExtentSize"));
			stats.put(STAT_NUM_INDEX, temp.get("nindexes"));
			stats.put(STAT_TOTAL_INDEX_SIZE, temp.get("totalIndexSize"));
			stats.put(STAT_INDEX_SIZES, temp.get("indexSizes"));
			return stats;
		} else {
			LOGGER.warn("Collection " + collectionName + " does not exist");
			Map<String, Object> errMap = createMap();
			errMap.put(STAT_ERROR, "Empty");
			return errMap;
		}
	}

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
	public Map<String, Object> getStatistics(String[] keys) {
		if (keys == null)
			return getStatistics();
		Map<String, Object> selectedStats = createMap();
		for (String collName : db.getCollectionNames()) {
			Map<String, Object> stats = getStats(collName);
			Map<String, Object> temp = createMap();
			for (String key : keys) {
				if (stats.get(key) != null)
					temp.put(key, stats.get(key));
			}
			selectedStats.put(collName, temp);
		}
		selectedStats.put(STAT_TIME_STAMP, System.currentTimeMillis());

		return selectedStats;
	}

	public static void main(String args[]) throws UnknownHostException,
			MongoException {
		StatsMongoCollectionGatherer collProps = new StatsMongoCollectionGatherer(
				new Mongo(), "enkive", "collService", "cronExpression");
		System.out.println(collProps.getStatistics());
		String[] keys = { STAT_TYPE, STAT_NAME, STAT_DATA_SIZE, };
		System.out.println(collProps.getStatistics(keys));
	}
}
