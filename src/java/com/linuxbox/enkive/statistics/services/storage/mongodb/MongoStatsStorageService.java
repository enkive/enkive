package com.linuxbox.enkive.statistics.services.storage.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.VarsMaker;
import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoStatsStorageService extends VarsMaker implements
		StatsStorageService {
	private static DBCollection coll;

	private static DB db;
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.services.storage.mongodb");
	private static Mongo m;

	public MongoStatsStorageService(Mongo mongo, String dbName, String collectionName) {
		m = mongo;
		db = m.getDB(dbName);
		coll = db.getCollection(collectionName);
		LOGGER.info("StorageService successfully created");
	}

	@Override
	public void storeStatistics(Set<Map<String, Object>> dataSet)
			throws StatsStorageException {
		for (Map<String, Object> map : dataSet) {
			coll.insert(new BasicDBObject(map));
		}
	}

	@Override
	public void storeStatistics(String service, RawStats data)
			throws StatsStorageException {
		Map<String, Object> result = createMap();
		result.put(STAT_GATHERER_NAME, service);
		result.put(STAT_TIMESTAMP, data.getTimestamp());
		result.putAll(data.getStatsMap());
		coll.insert(new BasicDBObject(result));
	}
}
