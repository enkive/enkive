package com.linuxbox.enkive.statistics.storage.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_COLLECTION;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.AbstractStatsService;
import com.linuxbox.enkive.statistics.gathering.StatsGatherer;
import com.linuxbox.enkive.statistics.storage.StatsStorageException;
import com.linuxbox.enkive.statistics.storage.StatsStorageService;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/* From StatsMongoStorageTest.java >>
 * ERIC:
 * So Mongo will try to store Longs as Ints if they are small enough
 * (within the range of integers). So we cannot garrantee a type on
 * our longs, however, this shouldn't be too much of a problem if you
 * just check the type. OR if we figure out a way to get mongo to store
 * & retrieve all longs as longs. I'm not sure how to do that so a big
 * improvement for the statistics package would be to figure that out.
 * 
 * Solution for timestamps? (STILL UNRESOLVED FOR OTHERS)
 * Lee gave me a good idea: Store timestamps as dates then do all comparisons using getTime()
 */
public class MongoStatsStorageService extends AbstractStatsService implements StatsStorageService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");

	private static Mongo m;
	private static DB db;
	private static DBCollection coll;
	String serviceName;
	Map<String, StatsGatherer> statisticsServices;

	public MongoStatsStorageService() {
		try {
			m = new Mongo();
		} catch (UnknownHostException e) {
			LOGGER.fatal("Mongo has failed: Unknown Host", e);
		} catch (MongoException e) {
			LOGGER.fatal("Mongo has failed: Mongo Execption", e);
		}
		db = m.getDB("enkive");
		statisticsServices = new HashMap<String, StatsGatherer>(); // changed
																	// StatsServices
																	// to
																	// StatsGatherer
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
	}

	public MongoStatsStorageService(Mongo mongo, String dbName) {
		m = mongo;
		db = m.getDB(dbName);
		statisticsServices = new HashMap<String, StatsGatherer>();
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
	}

	// ERIC: abstract void MongoException JSONException
	@Override
	public void storeStatistics(Set<Map<String, Object>> dataSet) {
		for (Map<String, Object> map : dataSet) {
			Map<String, Object> data = createMap();
			for (String key : map.keySet()) {
				data.put(key, map.get(key));
			}
			coll.insert(new BasicDBObject(data));
		}
	}

	@Override
	public void storeStatistics(String service, Date timestamp,
			Map<String, Object> data) throws StatsStorageException {
		BasicDBObject result = new BasicDBObject(data);
		result.put(STAT_SERVICE_NAME, service);
		result.put(STAT_TIME_STAMP, timestamp.getTime());
		coll.insert(result);
	}
}
