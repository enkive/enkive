package com.linuxbox.enkive.statistics.storage.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_COLLECTION;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;

import com.linuxbox.enkive.statistics.gathering.StatsMongoMsgGatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoDBGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.linuxbox.enkive.statistics.services.AbstractService;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.storage.StatsStorageException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/* 
 * Mongo will try to store Longs as Ints if they are small enough
 * (within the range of integers). So we cannot garrantee a type on
 * our longs, however, this shouldn't be too much of a problem if you
 * just check the type. OR if we figure out a way to get mongo to store
 * & retrieve all longs as longs. I'm not sure how to do that so a big
 * improvement for the statistics package would be to figure that out.
 * 
 * Solution for timestamps? (STILL UNRESOLVED)
 * Lee gave me a good idea: possibly Store timestamps as dates then do all comparisons using getTime()
 */
public class MongoStatsStorageService extends AbstractService implements
		StatsStorageService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");

	private static Mongo m;
	private static DB db;
	private static DBCollection coll;

	public MongoStatsStorageService() {
		try {
			m = new Mongo();
		} catch (UnknownHostException e) {
			LOGGER.fatal("Mongo has failed: Unknown Host", e);
		} catch (MongoException e) {
			LOGGER.fatal("Mongo has failed: Mongo Execption", e);
		}
		db = m.getDB("enkive");
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
	}

	public MongoStatsStorageService(Mongo mongo, String dbName) {
		m = mongo;
		db = m.getDB(dbName);
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void storeStatistics(Set<Map<String, Object>> dataSet)
			throws StatsStorageException {
		for (Map<String, Object> map : dataSet) {
			for (String key : map.keySet()) {
				storeStatistics(key, (Map<String, Object>) map.get(key));
			}
		}
	}

	@Override
	public void storeStatistics(String service, Map<String, Object> data)
			throws StatsStorageException {
		Map<String, Object> result = createMap();
		result.put(STAT_SERVICE_NAME, service);
		result.putAll(data);
		coll.insert(new BasicDBObject(result));
	}

	public static void main(String args[]) throws UnknownHostException,
			MongoException, GathererException, StatsStorageException,
			SchedulerException, ParseException {
		MongoStatsStorageService storage = new MongoStatsStorageService();
		AbstractGatherer dbProp = new StatsMongoDBGatherer(m, "enkive", "SERVICENAME", "CRONEXPRESSION");
		AbstractGatherer collProp = new StatsMongoCollectionGatherer(m,
				"enkive", "SERVICENAME", "CRONEXPRESSION");
		AbstractGatherer runProp = new StatsRuntimeGatherer();
		AbstractGatherer attProp = new StatsMongoAttachmentsGatherer(m,
				"enkive", STAT_STORAGE_COLLECTION, "SERVICENAME", "CRONEXPRESSION");
		AbstractGatherer msgStatProp = new StatsMongoMsgGatherer(m, "enkive",
				STAT_STORAGE_COLLECTION, "SERVICENAME", "CRONEXPRESSION");

		HashMap<String, AbstractGatherer> map = new HashMap<String, AbstractGatherer>();

		map.put("DatabaseStatsService", dbProp);
		map.put("CollStatsService", collProp);
		map.put("RuntimeStatsService", runProp);
		map.put("AttachstatsService", attProp);
		map.put("msgStatStatsService", msgStatProp);
		StatsGathererService gatherer = new StatsGathererService(map);
		storage.storeStatistics(gatherer.gatherStats());
	}
}
