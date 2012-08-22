package com.linuxbox.enkive.statistics.services.retrieval.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_ID;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.statistics.StatsFilter;
import com.linuxbox.enkive.statistics.StatsQuery;
import com.linuxbox.enkive.statistics.VarsMaker;
import com.linuxbox.enkive.statistics.services.StatsRetrievalService;
import com.linuxbox.enkive.statistics.services.retrieval.StatsRetrievalException;
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

	private DBObject getQuery(StatsQuery query) {
		DBObject mongoQuery = new BasicDBObject();
		DBObject time = new BasicDBObject();
		if(query.startTimestamp != null){
			time = new BasicDBObject();
			time.put("$gte", query.startTimestamp);
			mongoQuery.put(STAT_TIMESTAMP + "." + GRAIN_MIN, time);
		}
		if(query.endTimestamp != null){
			time = new BasicDBObject();
			time.put("$lt", query.endTimestamp);
			mongoQuery.put(STAT_TIMESTAMP + "." + GRAIN_MAX, time);	
		}

		if(query.grainType != null){
			if(query.grainType == 0){
				mongoQuery.put(GRAIN_TYPE, null);
			} else {
				mongoQuery.put(GRAIN_TYPE, query.grainType);
			}
		}
		
		if(query.gathererName != null){
			mongoQuery.put(STAT_GATHERER_NAME, query.gathererName);
		}
		return mongoQuery;
	}
	
	/**
	 * preforms a query on the database based on a query map
	 * @param hmap - the formatted query map
	 * @return the query results as a set of maps
	 */
	private Set<DBObject> getQuerySet(StatsQuery query) {
		Set<DBObject> result = new HashSet<DBObject>();
		result.addAll(coll.find(getQuery(query)).toArray());
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
	
	@SuppressWarnings("unchecked")
	private void addMapToList(DBObject entry, List<Map<String, Object>> stats){
		stats.add(entry.toMap());
	}

	@Override
	public Set<Map<String, Object>> queryStatistics() {
		Set<Map<String, Object>> result = new HashSet<Map<String, Object>>();
		for (DBObject entry : coll.find()) {
			addMapToSet(entry, result);
		}

		return result;
	}
	
	@Override
	public Set<Map<String, Object>> queryStatistics(StatsQuery query) {
		Set<Map<String, Object>> result = new HashSet<Map<String, Object>>();
		for (DBObject entry : getQuerySet(query)) {
			addMapToSet(entry, result);
		}
		return result;
	}

	private DBObject getFilter(StatsFilter filter){
		return new BasicDBObject(filter.keys);
	}
	
	//list to garrantee order
	public List<Map<String, Object>> queryStatistics(
			List<StatsQuery> queryList,
			List<StatsFilter> filterList)
			throws StatsRetrievalException {
		List<DBObject> allStats = new LinkedList<DBObject>();
		for (StatsQuery queryObject : queryList) {
			DBObject query  = getQuery(queryObject);
			DBObject filter = null;
			if(filterList != null && !filterList.isEmpty()){
				for(StatsFilter filterObject : filterList){
					if(filterObject.gathererName.equals(queryObject.gathererName)){
						filter = getFilter(filterObject);
						break;
					}
				}
			}			
			
			if(filter != null){
				allStats.addAll(coll.find(query, filter).sort(new BasicDBObject(STAT_TIMESTAMP + "." + GRAIN_MAX, 1)).toArray());
			} else {
				allStats.addAll(coll.find(query).toArray());
			}
		}
		List<Map<String, Object>> result = new LinkedList<Map<String, Object>>();
		for (DBObject entry : allStats) {
			addMapToList(entry, result);
		}
		return result;
	}
	
	@Override
	public List<Map<String, Object>> queryStatistics(
			Map<String, Map<String, Object>> queryMap,
			Map<String, Map<String, Object>> filterMap)
			throws StatsRetrievalException {
		Set<DBObject> allStats = new HashSet<DBObject>();
		for (String gathererName : queryMap.keySet()) {
			BasicDBObject query = new BasicDBObject();
			query.put(STAT_GATHERER_NAME, gathererName);
			query.putAll(queryMap.get(gathererName));
			if (filterMap.get(gathererName) != null
					&& !filterMap.get(gathererName).isEmpty()) {
				BasicDBObject filter = new BasicDBObject(
						filterMap.get(gathererName));
				allStats.addAll(coll.find(query, filter).sort(new BasicDBObject(STAT_TIMESTAMP + "." + GRAIN_MAX, 1)).toArray());
			} else {
				allStats.addAll(coll.find(query).sort(new BasicDBObject(STAT_TIMESTAMP + "." + GRAIN_MAX, 1)).toArray());
			}
		}
		List<Map<String, Object>> result = new LinkedList<Map<String, Object>>();
		for (DBObject entry : allStats) {
			addMapToList(entry, result);
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
