package com.linuxbox.enkive.statistics.services.storage.mongodb;

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

import com.linuxbox.enkive.statistics.AbstractCreator;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.GathererInterface;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoDBGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoMsgGatherer;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoStatsStorageService extends AbstractCreator implements
		StatsStorageService {
	private static DBCollection coll;

	private static DB db;
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.services.storage.mongodb");
	private static Mongo m;

	public static void main(String args[]) throws UnknownHostException,
			MongoException, GathererException, StatsStorageException,
			SchedulerException, ParseException {
		MongoStatsStorageService storage = new MongoStatsStorageService(
				new Mongo(), "enkive");
		GathererInterface dbProp = new StatsMongoDBGatherer(m, "enkive",
				"SERVICENAME", "CRONEXPRESSION");
		GathererInterface collProp = new StatsMongoCollectionGatherer(m,
				"enkive", "SERVICENAME", "CRONEXPRESSION");
		GathererInterface runProp = new StatsRuntimeGatherer("SERVICENAME",
				"CRONEXPRESSION");
		GathererInterface attProp = new StatsMongoAttachmentsGatherer(m,
				"enkive", STAT_STORAGE_COLLECTION, "SERVICENAME",
				"CRONEXPRESSION");
		GathererInterface msgStatProp = new StatsMongoMsgGatherer(m, "enkive",
				STAT_STORAGE_COLLECTION, "SERVICENAME", "CRONEXPRESSION");

		HashMap<String, GathererInterface> map = new HashMap<String, GathererInterface>();

		map.put("DatabaseStatsService", dbProp);
		map.put("CollStatsService", collProp);
		map.put("RuntimeStatsService", runProp);
		map.put("AttachstatsService", attProp);
		map.put("msgStatStatsService", msgStatProp);
		StatsGathererService gatherer = new StatsGathererService(map);
		storage.storeStatistics(gatherer.gatherStats());
	}

	public MongoStatsStorageService(Mongo mongo, String dbName) {
		m = mongo;
		db = m.getDB(dbName);
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
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
	public void storeStatistics(String service, Map<String, Object> data)
			throws StatsStorageException {
		Map<String, Object> result = createMap();
		result.put(STAT_SERVICE_NAME, service);
		result.putAll(data);
		coll.insert(new BasicDBObject(result));
	}
}
