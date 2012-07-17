package com.linuxbox.enkive.statistics.services.retrieval.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_ID;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.statistics.VarsMaker;
import com.linuxbox.enkive.statistics.services.StatsRetrievalService;
import com.linuxbox.enkive.statistics.services.retrieval.StatsRetrievalException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoStatsRetrievalService extends VarsMaker implements
		StatsRetrievalService {
	private static DBCollection coll;

	private static DB db;
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.services.retrieval.mongodb");
	private static Mongo m;

	public MongoStatsRetrievalService(Mongo mongo, String dbName, String collectionName) {
		m = mongo;
		db = m.getDB(dbName);
		coll = db.getCollection(collectionName);
		LOGGER.info("RetrievalService(Mongo, String) successfully created");
	}
	
	/**
	 * preforms a query on the database based on a date range the lower bound
	 * is treated as greater than or equal to and the upper bound is less than
	 * @param lowerDate - the lower date of the range
	 * @param upperDate - the upper date of the range
	 * @return the query results as a set of maps
	 */
	private Set<DBObject> getQuerySet(Date lowerDate, Date upperDate) {
		DBObject query = new BasicDBObject();
		DBObject time = new BasicDBObject();
		time.put("$gte", lowerDate);
		time.put("$lt", upperDate);
		query.put(STAT_TIME_STAMP, time);
		Set<DBObject> result = new HashSet<DBObject>();
		result.addAll(coll.find(query).toArray());
		return result;
	}

	/**
	 * preforms a query on the database based on a query map
	 * @param hmap - the formatted query map
	 * @return the query results as a set of maps
	 */
	private Set<DBObject> getQuerySet(Map<String, Map<String, Object>> hmap) {
		if (hmap == null) {// if null return all
			Set<DBObject> result = new HashSet<DBObject>();
			result.addAll(coll.find().toArray());
			return result;
		}
//TODO QUERY -- used to set up a single query on any gatherers at once
		Set<DBObject> result = new HashSet<DBObject>();
		BasicDBObject tempMap;
		BasicDBList or = new BasicDBList();
		for (String serviceName : hmap.keySet()) {
			tempMap = new BasicDBObject();
			if (hmap.get(serviceName) != null) {
				tempMap.putAll(hmap.get(serviceName));
			}

			tempMap.put(STAT_SERVICE_NAME, serviceName);
			or.add(tempMap);
		}
		BasicDBObject query = new BasicDBObject("$or", or);
		result.addAll(coll.find(query).toArray());
		return result;
	}

	/**
	 * preforms two querys: one on the query object and the other on the date range
	 * after done all objects not in the date range query are removed from the map
	 * object's query--in the date range the lower bound is treated as greater than or 
	 * equal to and the upper bound is less than
	 * @param queryMap -a map in the following format: {GathererName:{stat1:val1, stat2:val2,...}...}
	 * @param lowerDate - the lower bound date
	 * @param upperDate - the upper bound date
	 * @return the query results as a set of maps
	 */
	private Set<DBObject> getQuerySet(Map<String, Map<String, Object>> queryMap,
			Date lowerDate, Date upperDate) {
		Set<DBObject> hMapSet = getQuerySet(queryMap);
		Set<DBObject> dateSet = getQuerySet(lowerDate, upperDate);
		Set<DBObject> result = new HashSet<DBObject>();

		for (DBObject dateDBObj : dateSet) {
			for (DBObject mapDBObj : hMapSet) {
				if (mapDBObj.get(MONGO_ID).equals(dateDBObj.get(MONGO_ID))) {
					result.add(mapDBObj);
				}
			}
		}
		return result;
	}

	/**
	 * Takes a DBObject, extracts the map from it, and inserts that map into
	 * the given set. NOTE: warnings are suppressed because they are being
	 * activated because the actual type of the map is not specified in the
	 * dbObject but it should not matter so long as it conforms to Mongo's 
	 * key:value format
	 * @param entry -dbObject to extract map from
	 * @param stats -set to add map to
	 */
	@SuppressWarnings("unchecked")
	private void addMapToSet(DBObject entry, Set<Map<String, Object>> stats){
		stats.add(entry.toMap());
	}
	
	@Override
	public Set<Map<String, Object>> directQuery() {
		Set<Map<String, Object>> result = new HashSet<Map<String, Object>>();
		for (DBObject entry : coll.find().toArray()) {
			addMapToSet(entry, result);
		}
		return result;
	}

	@Override
	public Set<Map<String, Object>> directQuery(Map<String, Object> query) {
		Set<Map<String, Object>> result = new HashSet<Map<String, Object>>();
		if (query != null) {
			for (DBObject entry : coll.find(new BasicDBObject(query))) {
				addMapToSet(entry, result);
			}
		} else {
			return directQuery();
		}
		return result;
	}

	@Override
	public Set<Map<String, Object>> queryStatistics()
			throws StatsRetrievalException {
		return queryStatistics(null, null, null);
	}

	@Override
	public Set<Map<String, Object>> queryStatistics(Date startingTimestamp,
			Date endingTimestamp) throws StatsRetrievalException {
		return queryStatistics(null, startingTimestamp, endingTimestamp);
	}

	@Override
	public Set<Map<String, Object>> queryStatistics(
			Map<String, Map<String, Object>> stats)
			throws StatsRetrievalException {
		return queryStatistics(stats, null, null);
	}

	@Override
	public Set<Map<String, Object>> queryStatistics(
			Map<String, Map<String, Object>> hmap, Date lower, Date upper) {
		Set<Map<String, Object>> result = new HashSet<Map<String, Object>>();
		if (lower == null) {
			lower = new Date(0L);
		}
		if (upper == null) {
			upper = new Date();
		}

		for (DBObject entry : getQuerySet(hmap, lower, upper)) {
			addMapToSet(entry, result);
		}

		return result;
	}

	@Override
	public Set<Map<String, Object>> queryStatistics(
			Map<String, Map<String, Object>> queryMap,
			Map<String, Map<String, Object>> filterMap)
			throws StatsRetrievalException {
		Set<DBObject> allStats = new HashSet<DBObject>();
		for (String serviceName : queryMap.keySet()) {
			BasicDBObject query = new BasicDBObject();
			query.put(STAT_SERVICE_NAME, serviceName);
			query.putAll(queryMap.get(serviceName));
			if (filterMap.get(serviceName) != null
					&& !filterMap.get(serviceName).isEmpty()) {
				BasicDBObject filter = new BasicDBObject(
						filterMap.get(serviceName));
				allStats.addAll(coll.find(query, filter).toArray());
			} else {
				allStats.addAll(coll.find(query).toArray());
			}
			
		}
		Set<Map<String, Object>> result = new HashSet<Map<String, Object>>();
		for (DBObject entry : allStats) {
			addMapToSet(entry, result);
		}
		return result;
	}

	@Override
	public void remove(Set<Object> set) throws StatsRetrievalException {
		if (set != null) {// not null
			if (!set.isEmpty()) { // not empty
				for (Object id : set) {
					if (id instanceof ObjectId) {
						Map<String, ObjectId> map = new HashMap<String, ObjectId>();
						map.put(MONGO_ID, (ObjectId) id);
						coll.remove(new BasicDBObject(map));
					}
				}
			}
		}
	}
}
