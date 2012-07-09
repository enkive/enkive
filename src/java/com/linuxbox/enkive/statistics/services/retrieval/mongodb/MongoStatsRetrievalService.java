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

import com.linuxbox.enkive.statistics.AbstractCreator;
import com.linuxbox.enkive.statistics.services.StatsRetrievalService;
import com.linuxbox.enkive.statistics.services.retrieval.StatsRetrievalException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

// NOAH: I see the string "_id" in this file and in other files quite a bit. Seems like a good use of a constant.

// NOAH: As I've said in other files, I think these sets/lists/maps of other sets/lists/maps need to be documented so the reader knows what they contain. These should be in JavaDoc comments.

public class MongoStatsRetrievalService extends AbstractCreator implements
		StatsRetrievalService {
	private static DBCollection coll;

	private static DB db;
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.services.retrieval.mongodb");
	private static Mongo m;

	Map<String, Map<String, Object>> statisticsServices;

	public MongoStatsRetrievalService() {
		try {
			m = new Mongo();
		} catch (UnknownHostException e) {
			// NOAH: is there something you think needs doing? If so, document
			// the TODO more fully. Otherwise, remove. Applies here and
			// elsewhere.
			//
			// TODO
			LOGGER.fatal("Mongo has failed: Unknown Host", e);
		} catch (MongoException e) {
			// TODO
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
			HashMap<String, Map<String, Object>> statisticsServices) {
		m = mongo;
		db = m.getDB(dbName);
		// statsServices needs to be in format:
		// serviceName [...statnames to retrieve...]
		this.statisticsServices = statisticsServices;
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
		LOGGER.info("RetrievalService(Mongo, String, HashMap) successfully created");
	}

	// NOAH: again, buildSet is a bad name. Build a set that does what?
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

	private Set<DBObject> buildSet(Map<String, Map<String, Object>> hmap) {
		if (hmap == null) {// if null return all
			Set<DBObject> result = new HashSet<DBObject>();
			result.addAll(coll.find().toArray());
			return result;
		}

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

	private Set<DBObject> buildSet(Map<String, Map<String, Object>> hMap,
			long lower, long upper) {
		Set<DBObject> hMapSet = buildSet(hMap);
		Set<DBObject> dateSet = buildSet(lower, upper);
		Set<DBObject> bothSet = new HashSet<DBObject>();

		for (DBObject dateDBObj : dateSet) {
			for (DBObject mapDBObj : hMapSet) {
				if (mapDBObj.get("_id").equals(dateDBObj.get("_id"))) {
					bothSet.add(mapDBObj);
				}
			}
		}
		return bothSet;
	}

	// NOAH: The suppress warnings is necessary because DBObject returns a map
	// w/o key and value types specified? If so, let's document why we're
	// suppressing warnings in such situations.
	@SuppressWarnings("unchecked")
	@Override
	public Set<Map<String, Object>> directQuery(Map<String, Object> query) {
		Set<Map<String, Object>> allStats = new HashSet<Map<String, Object>>();
		for (DBObject entry : coll.find(new BasicDBObject(query)).toArray()) {
			allStats.add(entry.toMap());
		}
		return allStats;
	}

	// assuming statName is service name
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

	@SuppressWarnings("unchecked")
	@Override
	public Set<Map<String, Object>> queryStatistics(
			Map<String, Map<String, Object>> hmap, Date lower, Date upper) {
		Set<Map<String, Object>> allStats = new HashSet<Map<String, Object>>();
		if (lower == null) {
			lower = new Date(0L);
		}
		if (upper == null) {
			upper = new Date();
		}
		for (DBObject entry : buildSet(hmap, lower.getTime(), upper.getTime())) {
			allStats.add(entry.toMap());
		}
		return allStats;
	}

	// for use in the servlet
	@SuppressWarnings("unchecked")
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
			result.add(entry.toMap());
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
						map.put("_id", (ObjectId) id);
						coll.remove(new BasicDBObject(map));
					}
				}
			}
		}
	}
}
