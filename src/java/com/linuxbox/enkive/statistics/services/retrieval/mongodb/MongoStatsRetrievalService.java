package com.linuxbox.enkive.statistics.services.retrieval.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_COLLECTION;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_DB;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.statistics.services.AbstractService;
import com.linuxbox.enkive.statistics.services.StatsRetrievalService;
import com.linuxbox.enkive.statistics.services.retrieval.StatsRetrievalException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoStatsRetrievalService extends AbstractService implements
		StatsRetrievalService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.services.retrieval.mongodb");

	private static Mongo m;
	private static DB db;
	private static DBCollection coll;
	Map<String,Map<String,Object>> statisticsServices;

	public MongoStatsRetrievalService() {
		try {
			m = new Mongo();
		} catch (UnknownHostException e) {
			//TODO
			LOGGER.fatal("Mongo has failed: Unknown Host", e);
		} catch (MongoException e) {
			//TODO
			LOGGER.fatal("Mongo has failed: Mongo Execption", e);
		}
		db = m.getDB(STAT_STORAGE_DB);
		statisticsServices = null;
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
		LOGGER.info("RetrievalService() successfully created");
	}

	public MongoStatsRetrievalService(Mongo mongo, String dbName) {
		m = mongo;
		db = m.getDB(dbName);
		statisticsServices = null;
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
		LOGGER.info("RetrievalService(Mongo, String) successfully created");
	}

	public MongoStatsRetrievalService(Mongo mongo, String dbName,
			HashMap<String,Map<String,Object>> statisticsServices) {
		m = mongo;
		db = m.getDB(dbName);
		// statsServices needs to be in format:
		// serviceName [...statnames to retrieve...]
		this.statisticsServices = statisticsServices;
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
		LOGGER.info("RetrievalService(Mongo, String, HashMap) successfully created");
	}
	
	private Set<DBObject> buildSet(long lower, long upper) {
		DBObject query = new BasicDBObject();
		DBObject time = new BasicDBObject();
		time.put("$gte", lower);
		time.put("$lt", upper);
		query.put(STAT_TIME_STAMP, time);
		Set<DBObject> result = new HashSet<DBObject>();
		result.addAll(coll.find(query).toArray());
		return result;
	}
	
	//TODO test & use
	private Set<DBObject> buildSet(Map<String, Map<String, Object>> hmap) {
		if (hmap == null) {//if null return all
			Set<DBObject> result = new HashSet<DBObject>();
			result.addAll(coll.find().toArray());
			return result;
		}

		BasicDBObject query = new BasicDBObject();
		Set<DBObject> result = new HashSet<DBObject>();
		for (String serviceName : hmap.keySet()) {
			if(hmap.get(serviceName) != null)
				query.putAll(hmap.get(serviceName));
			
			query.put(STAT_SERVICE_NAME, serviceName);			
		}
//TODO		System.out.println("coll.find: " + coll.find(query).toArray());
		result.addAll(coll.find(query).toArray());
		return result;
	}

	private Set<DBObject> buildSet(Map<String,Map<String,Object>> hMap, long lower,
			long upper) {
		Set<DBObject> hMapSet = buildSet(hMap);
		Set<DBObject> dateSet = buildSet(lower, upper);
		Set<DBObject> bothSet = new HashSet<DBObject>();

		for (DBObject dateDBObj : dateSet) {
			for (DBObject mapDBObj : hMapSet) {
				if(mapDBObj.get("_id").equals(dateDBObj.get("_id"))){
					bothSet.add(mapDBObj);
				}
			}
		}
		return bothSet;
	}

	// assuming statName is service name
	@Override
	public Set<Map<String, Object>> queryStatistics()
			throws StatsRetrievalException {
		return queryStatistics(null, null, null);
	}

	@Override
	public Set<Map<String, Object>> queryStatistics(Map<String,Map<String,Object>> stats)
			throws StatsRetrievalException {
		return queryStatistics(stats, null, null);
	}

	@Override
	public Set<Map<String, Object>> queryStatistics(Date startingTimestamp,
			Date endingTimestamp) throws StatsRetrievalException {
		return queryStatistics(null, startingTimestamp, endingTimestamp);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Map<String, Object>> queryStatistics(Map<String, Map<String, Object>> hmap, Date lower, Date upper){
		Set<Map<String, Object>> allStats = new HashSet<Map<String, Object>>();
		if(lower == null){
			lower = new Date(0L);
		}
		if(upper == null){
			upper = new Date();
		}
		for (DBObject entry : buildSet(hmap, lower.getTime(), upper.getTime())) {
			allStats.add(entry.toMap());
		}
		return allStats;
	}
	
	public Set<Map<String, Object>> directQuery(Map<String, Object> query){
		Set<Map<String, Object>> allStats = new HashSet<Map<String, Object>>();
		System.out.println("Query: " + new BasicDBObject(query));
		for (DBObject entry : coll.find(new BasicDBObject(query)).toArray()) {
			allStats.add(entry.toMap());
		}
		return allStats;
	}

	public void remove(Set<Object> set) throws StatsRetrievalException{
		if(set != null){//not null
			if(!set.isEmpty()){ //not empty
				for(Object id: set){
					if(id instanceof ObjectId){
						Map<String, ObjectId> map = new HashMap<String, ObjectId>();
						map.put("_id", (ObjectId)id);
						coll.remove(new BasicDBObject(map));
					}
				}
			}
		}
	}

	public static void main(String args[]) throws StatsRetrievalException {
		System.out.println("Starting Retrieval Test: ");
		MongoStatsRetrievalService retriever = new MongoStatsRetrievalService();
		Date lower = null;// 1337198505000L);
		Date upper = new Date();//current time
		Map<String, Object> keyVals = new HashMap<String, Object>();
		keyVals.put("dataSize", 25442832);
		keyVals.put("type", "database");
		Map<String, Map<String, Object>> serviceKeyMap = new HashMap<String, Map<String, Object>>();
		serviceKeyMap.put("DatabaseStatsService", keyVals);
		System.out.println("\nretriever.query(map<str, map<str,obj>>");
		for (Map<String, Object> map : retriever.queryStatistics(serviceKeyMap, lower, upper)) {
			System.out.println(map);
		}
		
		System.out.println("Finished Retrieval Tests");
	}
}
