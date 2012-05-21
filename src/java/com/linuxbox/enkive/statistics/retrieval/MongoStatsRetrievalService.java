package com.linuxbox.enkive.statistics.retrieval;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_COLLECTION;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.AbstractStatsService;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoStatsRetrievalService extends AbstractStatsService implements StatsRetrievalService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");

	private static Mongo m;
	private static DB db;
	private static DBCollection coll;
	Map<String, String[]> statisticsServices;

	public MongoStatsRetrievalService() {
		try {
			m = new Mongo();
		} catch (UnknownHostException e) {
			LOGGER.fatal("Mongo has failed: Unknown Host", e);
		} catch (MongoException e) {
			LOGGER.fatal("Mongo has failed: Mongo Execption", e);
		}
		db = m.getDB("enkive");
		statisticsServices = null;
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
	}

	public MongoStatsRetrievalService(Mongo mongo, String dbName) {
		m = mongo;
		db = m.getDB(dbName);
		statisticsServices = null;
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
	}

	public MongoStatsRetrievalService(Mongo mongo, String dbName,
			HashMap<String, String[]> statisticsServices) {
		m = mongo;
		db = m.getDB(dbName);
		//statsServices needs to be in format:
		//serviceName [...statnames to retrieve...]
		this.statisticsServices = statisticsServices;
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
	}

	private Set<DBObject> buildSet() {
		Set<DBObject> result = new HashSet<DBObject>();		
		result.addAll(coll.find().toArray());
		return result;
	}

	private Set<DBObject> buildSet(long lower, long upper) {
		DBObject query = new BasicDBObject();
		DBObject time = new BasicDBObject();
		time.put("$lte", upper);
		time.put("$gte", lower);
		query.put(STAT_TIME_STAMP, time);
		Set<DBObject> result = new HashSet<DBObject>();
		result.addAll(coll.find(query).toArray());
		return result;
	}

	private Set<DBObject> buildSet(Map<String, String[]> hmap) {
		if (hmap == null) {
			return buildSet();
		}

		DBObject query = new BasicDBObject();
		DBObject keyFilter = new BasicDBObject();
		BasicDBList SetKey = new BasicDBList();
		for (String serviceName : hmap.keySet()) {
			Object temp = new BasicDBObject(STAT_SERVICE_NAME, serviceName);
			SetKey.add(temp);
			String[] keys = hmap.get(serviceName);
			if (keys != null) {
				for (String key : keys)
					keyFilter.put(key, 1);
			}
			if (!keyFilter.containsField(STAT_SERVICE_NAME))
				keyFilter.put(STAT_SERVICE_NAME, 1);
			if (!keyFilter.containsField(STAT_TIME_STAMP))
				keyFilter.put(STAT_TIME_STAMP, 1);
		}
		query.put("$or", SetKey.toArray());
		Set<DBObject> result = new HashSet<DBObject>();
		result.addAll(coll.find(query, keyFilter).toArray());
		return result;
	}

	private Set<DBObject> buildSet(Map<String, String[]> hMap,
			long lower, long upper) {
		Set<DBObject> hMapSet = buildSet(hMap);
		Set<DBObject> dateSet = buildSet(lower, upper);
		Set<DBObject> bothSet = new HashSet<DBObject>();

		for (DBObject dateDBObj : dateSet) {
			for (DBObject mapDBObj : hMapSet) {
				if (mapDBObj.get(STAT_SERVICE_NAME).equals(
						dateDBObj.get(STAT_SERVICE_NAME))
						&& mapDBObj.get(STAT_TIME_STAMP).equals(
								dateDBObj.get(STAT_TIME_STAMP)))
					bothSet.add(mapDBObj);
			}
		}

		return bothSet;
	}

	//assuming statName is service name 
	@Override
	public Set<Map<String,Object>> queryStatistics()
			throws StatsRetrievalException {
		Set<Map<String, Object>> allStats = createSet();
		for (DBObject entry : buildSet()) {
			Map<String, Object> map = entry.toMap();
			allStats.add(map);
		}
		return allStats;
	}
	
	@Override
	public Set<Map<String,Object>> queryStatistics(Map<String, String[]> stats)
			throws StatsRetrievalException {
		if(stats == null)
			return queryStatistics();
		
		Set<Map<String, Object>> allStats = new HashSet<Map<String, Object>>();
		for (DBObject entry : buildSet(stats)) {
			Map<String, Object> map = entry.toMap();
			allStats.add(map);
		}
		return allStats;
	}
	
	@Override
	public Set<Map<String,Object>> queryStatistics(Date startingTimestamp, Date endingTimestamp)
			throws StatsRetrievalException {
		Set<Map<String, Object>> allStats = new HashSet<Map<String, Object>>();
		for (DBObject entry : buildSet(startingTimestamp.getTime(), endingTimestamp.getTime())) {
			Map<String, Object> map = entry.toMap();
			allStats.add(map);
		}
		return allStats;
	}
	
	@Override
	public Set<Map<String,Object>> queryStatistics(Map<String, String[]> stats,
			Date startingTimestamp, Date endingTimestamp)
			throws StatsRetrievalException {
		if(startingTimestamp == null){
			startingTimestamp = new Date(0L);
		}
		if(endingTimestamp == null){
			endingTimestamp = new Date();
		}
		if(stats == null)
			return queryStatistics(startingTimestamp, endingTimestamp);
		
		Set<Map<String, Object>> allStats = new HashSet<Map<String, Object>>();
		for (DBObject entry : buildSet(stats, startingTimestamp.getTime(), endingTimestamp.getTime())) {
			Map<String, Object> map = entry.toMap();
			allStats.add(map);
		}
		return allStats;
	}
	
	public static void main(String args[]) throws StatsRetrievalException{
		System.out.println("Starting Retrieval Test: ");
		MongoStatsRetrievalService retriever = new MongoStatsRetrievalService();
		Map<String, String[]> services = new HashMap<String, String[]>();
		//TODO: make service names constants
		String[] names = {"_id", "numCollections", "dataSize", "timeStamp", "avgStat", "maxStat"};
		services.put("DatabaseStatsService", names);
		services.put("AttachmentsStatsService", names);
		Date lower = new Date(0L);//1337198505000L);
		Date upper = new Date();
		System.out.println("\nretriever.queryStatistics()");
		for(Map<String, Object> map : retriever.queryStatistics()){
			System.out.println(map);
		}
		System.out.println("\nretriever.queryStatistics(Date, Date)");
		for(Map<String, Object> map : retriever.queryStatistics(lower, upper)){
			System.out.println(map);
		}
		System.out.println("\nretriever.queryStatistics(map)");
		for(Map<String, Object> map : retriever.queryStatistics(services)){
			System.out.println(map);
		}
		System.out.println("\nretriever.queryStatistics(map, Date, Date)");
		for(Map<String, Object> map : retriever.queryStatistics(services, lower, upper)){
			System.out.println(map);
		}
		System.out.println("\nretriever.queryStatistics(map, null, Date)");
		for(Map<String, Object> map : retriever.queryStatistics(services, null, upper)){
			System.out.println(map);
		}
		System.out.println("\nretriever.queryStatistics(map, null, Date)");
		for(Map<String, Object> map : retriever.queryStatistics(services, lower, null)){
			System.out.println(map);
		}
	
		System.out.println("Finished Retrieval Tests");
	}
}
