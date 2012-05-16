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

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;

public class StatsMongoCollectionProperties implements StatsGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");

	protected Mongo m;
	protected DB db;

	public StatsMongoCollectionProperties(Mongo m, String dbName) {
		this.m = m;
		db = m.getDB(dbName);
	}

	public BasicDBObject getAllStats() {
		Iterator<String> collNames = db.getCollectionNames().iterator();
		BasicDBObject allCollStats = new BasicDBObject();
		while (collNames.hasNext()) {
			String collName = collNames.next();
			allCollStats.put(collName, getStats(collName));
		}
		// NOTE: stat time is on upper level because I don't think we need the
		// precision of tracking it for every single collection's data (they
		// are all close enough to call them the same anyway)
		allCollStats.put(STAT_TIME_STAMP, System.currentTimeMillis());
		return allCollStats;
	}

	public BasicDBObject getStats(String collectionName) {
		if (db.collectionExists(collectionName)) {
			BasicDBObject stats = new BasicDBObject();
			BasicDBObject temp = db.getCollection(collectionName).getStats();
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
			return new BasicDBObject(STAT_ERROR, "Empty");
		}
	}

	public JSONObject getStatisticsJSON() {
		JSONObject result = new JSONObject(getAllStats());
		return result;
	}

	public JSONObject getStatisticsJSON(Map<String, String> map) {
		// TODO: Implement
		return getStatisticsJSON();
	}
}
