package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.MongoConstants.MONGO_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.MongoConstants.MONGO_COUNT;
import static com.linuxbox.enkive.statistics.MongoConstants.MONGO_INDEX_SIZES;
import static com.linuxbox.enkive.statistics.MongoConstants.MONGO_LAST_EXTENT_SIZE;
import static com.linuxbox.enkive.statistics.MongoConstants.MONGO_NS;
import static com.linuxbox.enkive.statistics.MongoConstants.MONGO_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.MongoConstants.MONGO_NUM_INDEX;
import static com.linuxbox.enkive.statistics.MongoConstants.MONGO_SIZE;
import static com.linuxbox.enkive.statistics.MongoConstants.MONGO_STORAGE_SIZE;
import static com.linuxbox.enkive.statistics.MongoConstants.MONGO_TOTAL_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ERROR;
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
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
public class StatsMongoCollectionGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering");

	protected Mongo m;
	protected DB db;

	public StatsMongoCollectionGatherer(Mongo m, String dbName, String serviceName, String schedule) {
		super(serviceName, schedule);
		this.m = m;
		db = m.getDB(dbName);
	}
	
	protected Map<String, Set<String>> keyBuilder(){
		Map<String, Set<String>> keys = new HashMap<String, Set<String>>();
		keys.put(STAT_TYPE, null);
//		keys.remove(STAT_NS);
		
		Set<String> generic = makeCreator(GRAIN_AVG, GRAIN_MAX, GRAIN_MIN);
		
		keys.put(STAT_TIME_STAMP, generic);
		keys.put(STAT_NUM_OBJS, generic);
		keys.put(STAT_AVG_OBJ_SIZE, generic);
		keys.put(STAT_DATA_SIZE, generic);
		keys.put(STAT_TOTAL_SIZE, generic);
		keys.put(STAT_NUM_EXTENT, generic);
		keys.put(STAT_LAST_EXTENT_SIZE, generic);
		keys.put(STAT_NUM_INDEX, generic);
		keys.put(STAT_TOTAL_INDEX_SIZE, generic);
		
		//TODO: embedded
		keys.put(STAT_INDEX_SIZES, null);
		
		return keys;
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
		selectedStats.put(STAT_SERVICE_NAME, attributes.getName());
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
