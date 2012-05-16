package com.linuxbox.enkive.statistics.storage.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_COLLECTION;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.statistics.StatsService;
import com.linuxbox.enkive.statistics.gathering.MongoMessageStatisticsService;
import com.linuxbox.enkive.statistics.gathering.StatsGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoAttachments;
import com.linuxbox.enkive.statistics.gathering.StatsMongoCollectionProperties;
import com.linuxbox.enkive.statistics.gathering.StatsMongoDBProperties;
import com.linuxbox.enkive.statistics.gathering.StatsMsgEntries;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeProperties;
import com.linuxbox.enkive.statistics.storage.StatsStorageException;
import com.linuxbox.enkive.statistics.storage.StatsStorageService;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

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
public class MongoStatsStorageService extends StatsService implements
		StatsStorageService {
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

	public MongoStatsStorageService(Mongo mongo, String dbName,
			HashMap<String, StatsGatherer> statisticsServices) {
		m = mongo;
		db = m.getDB(dbName);
		this.statisticsServices = statisticsServices;
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
	}

	public MongoStatsStorageService(Mongo mongo, String dbName,
			String serviceName, StatsGatherer service) {
		m = mongo;
		db = m.getDB(dbName);
		this.serviceName = serviceName;
		statisticsServices = new HashMap<String, StatsGatherer>();
		statisticsServices.put(serviceName, service);
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
	}

	public JSONObject getStatisticsJSON() throws JSONException {
		JSONObject results = new JSONObject();
		Iterator<Entry<String, StatsGatherer>> iterator = statisticsServices
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, StatsGatherer> pair = (Map.Entry<String, StatsGatherer>) iterator
					.next();
			results.append(pair.getKey(), pair.getValue().getStatisticsJSON());
		}
		return results;
	}

	// ERIC: abstract void MongoException JSONException
	public void storeData() throws MongoException, JSONException {
		DBObject data = (DBObject) JSON.parse((getStatisticsJSON().toString()));
		Iterator<String> keys = data.keySet().iterator();

		while (keys.hasNext()) {
			BasicDBObject result = new BasicDBObject();
			String outKey = keys.next();
			DBObject stats = (DBObject) ((BasicDBList) data.get(outKey))
					.iterator().next();
			Iterator<String> statsKeys = stats.keySet().iterator();

			result.put(STAT_SERVICE_NAME, outKey);
			while (statsKeys.hasNext()) {
				String inKey = statsKeys.next();
				String newKey = inKey;
				if (newKey.startsWith("$")) {
					newKey = newKey.replaceFirst("$", "-");
				}
				newKey = newKey.replace('.', '-');
				result.put(newKey, stats.get(inKey));
			}
			coll.insert(result);
		}
	}

	/**
	 * @param entryData
	 *            -one stored service's statistics document
	 * @return entryData as a JSONObject in the same format as the
	 *         getStatisticsJSON()
	 * @throws JSONException
	 */
	private JSONObject storageToJSON(DBObject entryData) throws JSONException {
		JSONObject statsData = new JSONObject();
		Iterator<String> entryIter = null;
		entryIter = entryData.keySet().iterator();
		while (entryIter.hasNext()) {
			String key = entryIter.next();
			statsData.put(key, entryData.get(key));
		}
		return statsData;
	}
	
	public List<DBObject> buildList() {
		return coll.find().toArray();
	}
	
	public List<DBObject> buildList(long lower, long upper) {
		DBObject query = new BasicDBObject();
		DBObject time = new BasicDBObject();
		time.put("$lte", upper);
		time.put("$gte", lower);
		query.put(STAT_TIME_STAMP, time);
		// System.out.println("query: " + query);
		return coll.find(query).toArray();
	}
	
	public List<DBObject> buildList(HashMap<String, String[]> hmap) {
		if (hmap == null) {
			System.out.println("HMAP IS Null");
			return buildList();
		}
		
		BasicDBObject query = new BasicDBObject();
		BasicDBObject keyFilter = new BasicDBObject();
		BasicDBList listKey = new BasicDBList();
		for(String serviceName: hmap.keySet()){
			Object temp = new BasicDBObject(STAT_SERVICE_NAME, serviceName);
			listKey.add(temp);
			String[] keys = hmap.get(serviceName);
			if(keys != null) {
				for(String key : keys)
					keyFilter.put(key, 1);
			}
			if (!keyFilter.containsField(STAT_SERVICE_NAME))
				keyFilter.put(STAT_SERVICE_NAME, 1);
			if (!keyFilter.containsField(STAT_TIME_STAMP))
				keyFilter.put(STAT_TIME_STAMP, 1);
		}
		query.put("$or", listKey.toArray());
		return coll.find(query, keyFilter).toArray();
	}
	
	public List<DBObject> buildList(HashMap<String, String[]> hMap,
			long lower, long upper) {
		List<DBObject> hMapList = buildList(hMap); 
		List<DBObject> dateList = buildList(lower, upper);
		List<DBObject> bothList = new LinkedList<DBObject>();
		
		for(DBObject dateDBObj: dateList){
			for(DBObject mapDBObj: hMapList){
				if(mapDBObj.get(STAT_SERVICE_NAME).equals(dateDBObj.get(STAT_SERVICE_NAME)) && mapDBObj.get(STAT_TIME_STAMP).equals(dateDBObj.get(STAT_TIME_STAMP)))
					bothList.add(mapDBObj);
			}
		}
		
		return bothList;
	}
	
	public Iterator<DBObject> buildIterator(long lower, long upper) {
		DBObject query = new BasicDBObject();
		DBObject time = new BasicDBObject();
		time.put("$lte", upper);
		time.put("$gte", lower);
		query.put(STAT_TIME_STAMP, time);
		// System.out.println("query: " + query);
		return coll.find(query).iterator();
	}

	public Iterator<DBObject> buildIterator(HashMap<String, String[]> hmap) {
		if (hmap == null) {
			System.out.println("HMAP IS Null");
			return coll.find().iterator();
		}

		BasicDBObject query = new BasicDBObject();
		BasicDBObject keyFilter = new BasicDBObject();
		Iterator<String> iter = hmap.keySet().iterator();
		String serviceName;
		BasicDBList listKey = new BasicDBList();
		while (iter.hasNext()) {
			serviceName = iter.next();
			Object temp = new BasicDBObject(STAT_SERVICE_NAME, serviceName);
			listKey.add(temp);
			String[] keys = hmap.get(serviceName);

			if (keys != null) {
				for (String key : keys) {
					keyFilter.put(key, 1);
				}
			}
			if (!keyFilter.containsField(STAT_SERVICE_NAME))
				keyFilter.put(STAT_SERVICE_NAME, 1);
			if (!keyFilter.containsField(STAT_TIME_STAMP))
				keyFilter.put(STAT_TIME_STAMP, 1);
		}
		query.put("$or", listKey.toArray());
		// System.out.println("query " + query);
		// System.out.println("keyFilter " + keyFilter);

		return coll.find(query, keyFilter).iterator();
	}

	// ERIC: if I just had a loop that went through the iterators created by my
	// other two implementations
	// of this method and compared one to the other that could allow me to
	// combine them:
	// ( if(found in both) add it to as list; else trash & move to next)>>return
	// an iterator of the combination
	public Iterator<DBObject> buildIterator(HashMap<String, String[]> hmap,
			long lower, long upper) {
		DBObject query = new BasicDBObject();
		DBObject time = new BasicDBObject();
		time.put("$lte", upper);
		time.put("$gte", lower);
		query.put(STAT_TIME_STAMP, time);

		if (hmap == null) {
			return coll.find().iterator();
		}
		BasicDBObject keyFilter = new BasicDBObject();
		Iterator<String> iter = hmap.keySet().iterator();
		BasicDBList listKey = new BasicDBList();
		while (iter.hasNext()) {
			serviceName = iter.next();
			Object temp = new BasicDBObject(STAT_SERVICE_NAME, serviceName);
			listKey.add(temp);
			String[] keys = hmap.get(serviceName);

			if (keys != null) {
				for (String key : keys) {
					keyFilter.put(key, 1);
				}
			}
			if (!keyFilter.containsField(STAT_SERVICE_NAME))
				keyFilter.put(STAT_SERVICE_NAME, 1);
			if (!keyFilter.containsField(STAT_TIME_STAMP))
				keyFilter.put(STAT_TIME_STAMP, 1);
		}
		query.put("$or", listKey.toArray());
		// System.out.println("query " + query);
		// System.out.println("keyFilter " + keyFilter);

		return coll.find(query, keyFilter).iterator();
	}

	public JSONObject retrieveStats(long lower, long upper)
			throws JSONException {
		Iterator<DBObject> dataIter = buildIterator(lower, upper);
		JSONObject allStats = new JSONObject();
		while (dataIter.hasNext()) {
			DBObject entry = dataIter.next();
			String entryServiceName = (String) entry.get(STAT_SERVICE_NAME);
			entry.removeField(STAT_SERVICE_NAME);
			allStats.put(entryServiceName, storageToJSON(entry));
		}
		return allStats;
	}

	public JSONObject retrieveStats() throws JSONException {
		Iterator<DBObject> dataIter = coll.find().iterator();
		JSONObject allStats = new JSONObject();
		while (dataIter.hasNext()) {
			DBObject entry = dataIter.next();
			String entryServiceName = (String) entry.get(STAT_SERVICE_NAME);
			entry.removeField(STAT_SERVICE_NAME);
			allStats.put(entryServiceName, storageToJSON(entry));
		}
		return allStats;
	}

	public JSONObject retrieveStats(HashMap<String, String[]> hmap)
			throws JSONException {
		Iterator<DBObject> dataIter = buildIterator(hmap);
		JSONObject allStats = new JSONObject();
		while (dataIter.hasNext()) {
			DBObject entry = dataIter.next();
			String entryServiceName = (String) entry.get(STAT_SERVICE_NAME);
			entry.removeField(STAT_SERVICE_NAME);
			allStats.put(entryServiceName, storageToJSON(entry));
		}
		return allStats;
	}

	public JSONObject retrieveStats(HashMap<String, String[]> hmap, long lower,
			long upper) throws JSONException {
		Iterator<DBObject> dataIter = buildIterator(hmap, lower, upper);
		JSONObject allStats = new JSONObject();
		while (dataIter.hasNext()) {
			DBObject entry = dataIter.next();
			String entryServiceName = (String) entry.get(STAT_SERVICE_NAME);
			entry.removeField(STAT_SERVICE_NAME);
			allStats.put(entryServiceName, storageToJSON(entry));
		}
		return allStats;
	}
	
	public JSONObject retrieveStats(HashMap<String, String[]> hMap, Date lower,
			Date upper) throws JSONException {
		if(lower == null){
			lower = new Date(0L);
		}
		if(upper == null){
			upper = new Date(System.currentTimeMillis());
		}
		if(hMap == null){
			retrieveStats(lower.getTime(), upper.getTime());
		}
		Iterator<DBObject> dataIter = buildIterator(hMap, lower.getTime(), upper.getTime());
		JSONObject allStats = new JSONObject();
		while (dataIter.hasNext()) {
			DBObject entry = dataIter.next();
			String entryServiceName = (String) entry.get(STAT_SERVICE_NAME);
			entry.removeField(STAT_SERVICE_NAME);
			allStats.put(entryServiceName, storageToJSON(entry));
		}
		return allStats;
	}


	// testing main() function
	public static void main(String args[]) throws JSONException {
		System.out.println("main");
		System.out.println("Starting...");
		Mongo m = null;
		try {
			m = new Mongo();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			System.exit(0);
		} catch (MongoException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		StatsGatherer prop1 = new StatsMongoDBProperties(m, "enkive");
		StatsGatherer prop2 = new StatsMongoCollectionProperties(m, "enkive");
		StatsGatherer prop3 = new StatsRuntimeProperties();
		StatsGatherer prop4 = new StatsMsgEntries();
		StatsGatherer prop5 = new StatsMongoAttachments(m, "enkive", "fs");
		StatsGatherer prop6 = new MongoMessageStatisticsService(m, "enkive",
				"emailMessages");
		HashMap<String, StatsGatherer> map = new HashMap<String, StatsGatherer>();
		map.put("DatabaseStatsService", prop1);
		map.put("CollectionStatsService", prop2);
		map.put("RuntimeStatsService", prop3);
		map.put("MsgEntriesStatsService", prop4);
		map.put("AttachmentsStatsService", prop5);
		map.put("BasicMsgStatistics", prop6);

		MongoStatsStorageService service = new MongoStatsStorageService(m,
				"enkive", map);
		System.out.println("Running...");
		try {
			service.storeData();
		} catch (MongoException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (JSONException e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("finished storage...");
		System.out.println("starting Retreival");
		String str = "RuntimeStatsService";
		String[] strArray = { "availableProcessors" };
		HashMap<String, String[]> hmap = new HashMap<String, String[]>();
		hmap.put(str, strArray);

		str = "MsgEntriesStatsService";
		hmap.put(str, null);
		JSONObject data1 = service.retrieveStats(hmap);
		JSONObject data2 = service.retrieveStats(0, 999999999999999999L);
		JSONObject data3 = service.retrieveStats(hmap, 0, 999999999999999L);
		System.out.println("printing results...");
		System.out.println("data1: " + data1);
		System.out.println("data2: " + data2);
		System.out.println("data3: " + data3);
		System.out.println("...finished.");
		// coll.drop();
	}

	@Override
	public void storeStatistics(String service, Date timestamp,
			Map<String, Object> data) throws StatsStorageException {
		BasicDBObject result = new BasicDBObject(data);
		result.put(STAT_SERVICE_NAME, service);
		result.put(STAT_TIME_STAMP, timestamp.getTime());
		coll.insert(result);
	}

	@Override
	public List<Object> queryStatistics(String statName,
			Date startingTimestamp, Date endingTimestamp)
			throws StatsStorageException {

		DBObject query = new BasicDBObject();
		DBObject time = new BasicDBObject();
		time.put("$lte", endingTimestamp.getTime());
		time.put("$gte", startingTimestamp.getTime());
		query.put(STAT_TIME_STAMP, time);
		if(statName != null)
			query.put(STAT_SERVICE_NAME, statName);

		List<Object> list = new LinkedList<Object>();
		for (DBObject entry : coll.find(query)) {
			list.add(entry);
		}
		return list;
	}
}
