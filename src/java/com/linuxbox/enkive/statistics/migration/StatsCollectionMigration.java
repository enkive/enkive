package com.linuxbox.enkive.statistics.migration;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.*;
import static com.linuxbox.enkive.statistics.StatsConstants.*;
import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.util.dbmigration.DBInfo;
import com.linuxbox.util.dbmigration.DBMigration;
import com.linuxbox.util.dbmigration.DBMigrationException;
import com.linuxbox.util.dbmigration.DBMigrator;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 * Migrates the stats data from 1.2 to 1.2.1 (cleans out duplicate entries and
 * re-inserts the sums then builds the weekly & monthly off the daily data)
 * 
 * @author noah
 * 
 */
public class StatsCollectionMigration {//extends DBMigration {
	public StatsCollectionMigration(Mongo m)//, StatsClient client)
			throws DBMigrationException {
//		super(null, 1, 12);
//		this.client = client;
		this.m = m;
		this.enkive = m.getDB("enkive");
	}
	
/*	public StatsCollectionMigration(DBMigrator migrator, int fromVersion,
			int toVersion, Mongo m, StatsClient client)
			throws DBMigrationException {
		super(migrator, fromVersion, toVersion);
		this.client = client;
		this.m = m;
		this.enkive = m.getDB("enkive");
	}	
*/	
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.migration");
//	StatsClient client;
	Mongo m;
	DB enkive;
	DBCollection metaColl;
	DBCollection statsCollNew;
	DBCollection statsCollOld;
	String currentVersion;
	
	/**
	 * run down a path and get the data map stored at the end
	 * @param statsMap- larger map to trace down
	 * @param path- path specifying data in statsMap
	 * @return the map at the end of the path
	 */
	private Map<String, Object> getData(Map<String, Object> statsMap,
			List<String> path) {
		for (String key : path) {
			statsMap = (Map<String, Object>) statsMap.get(key);
		}
		return statsMap;
	}

	/**
	 *  fix the daily data
		if we have the average for the daily then multiply it by 24 to get
		the sum
		else if we have the max & min for the daily then average them
		else store a -1 in the sum meaning no further consolidations may take place
	 * @param statsData
	 * @param methods
	 */
	private void fixDailyData(Map<String, Object> statsData, Collection<String> methods) {
		double sum = -1;
		if (methods.contains(CONSOLIDATION_AVG)) {
			sum = (Double) statsData.get(CONSOLIDATION_AVG) * 24;
		} else if (methods.contains(CONSOLIDATION_MAX)
				&& methods.contains(CONSOLIDATION_MIN)) {
			sum = ((Double) statsData.get(CONSOLIDATION_MAX) + (Double) statsData
					.get(CONSOLIDATION_MIN)) / 2;
		}
		
		statsData.put(CONSOLIDATION_SUM, sum);
	}

	/**
	 * mutates the given statsMap to make sure it has sums in the relevant places
	 * @param statsMap
	 */
	public void fixDailyStatsMap(Map<String, Object> statsMap) {
		for(String key: statsMap.keySet()){
			Object obj = statsMap.get(key);
			if(obj instanceof Map && !key.equals(STAT_TIMESTAMP)){
				Map<String, Object> nextMap = (Map<String, Object>)obj;
				Set<String> keySet = nextMap.keySet();
				if(!keySet.contains(CONSOLIDATION_SUM)){
					if(keySet.contains(CONSOLIDATION_AVG)){
						System.out.println("key: " + key);
						double sum = (Double) nextMap.get(CONSOLIDATION_AVG) * 24;
						nextMap.put(CONSOLIDATION_SUM, sum);
						System.out.println(key +" sum: " + sum);
					} else if(keySet.contains(CONSOLIDATION_MAX)) {
						double max = (Double) nextMap.get(CONSOLIDATION_MAX);
						double min = (Double) nextMap.get(CONSOLIDATION_MIN);
						double sum = (max + min)/2;
						nextMap.put(CONSOLIDATION_SUM, sum);
					} else {
						fixDailyStatsMap(nextMap);
					}
				}
			}
		}
	}
	
	
//	@Override
	public boolean migrate(DBInfo db) throws DBMigrationException {
		if (enkive.collectionExists("statistics")) {
			this.statsCollOld = enkive.getCollection("statistics");
		} else {
			LOGGER.error("could not find statistics collection");
			return false;
		}

		enkive.getCollection("statisticsV1_2_1").drop();
		this.statsCollNew = enkive.getCollection("statisticsV1_2_1");

		Integer[] validGrainTypes = {null, CONSOLIDATION_HOUR, CONSOLIDATION_DAY};
		DBObject goodEntriesQuery = new BasicDBObject(CONSOLIDATION_TYPE, new BasicDBObject("$in", validGrainTypes));
		
		List<DBObject> oldStatsData = statsCollOld.find(goodEntriesQuery, new BasicDBObject("_id", false)).toArray();
		
		for(DBObject statData: oldStatsData){
			Map<String, Object> statDataMap = statData.toMap();
			Integer  type = (Integer)statDataMap.get(CONSOLIDATION_TYPE);
			
			if(type != null && type == CONSOLIDATION_DAY){
				System.out.println("before: " + statDataMap);
				fixDailyStatsMap(statDataMap);
				System.out.println("after: " + statDataMap);
			}
			
			statsCollNew.insert(new BasicDBObject(statDataMap));
		}

		System.exit(1);
		
		//insert fixed statsData into the new collection one at a time
/*		for(DBObject statData: statsCollOld.find(goodEntriesQuery, new BasicDBObject("_id", false)).toArray()){
			Map<String, Object> statDataMap = statData.toMap();
			String gathererName = (String)statDataMap.get(STAT_GATHERER_NAME);
			Integer  type = (Integer)statDataMap.get(CONSOLIDATION_TYPE);
			if(!gathererName.equals("CollectionStatsGatherer") && type == CONSOLIDATION_DAY){
				GathererAttributes attributes = client.getAttributes(gathererName);
//				for(ConsolidationKeyHandler keyHandler: attributes.getKeys()){
				//	fixDailyData(getData(statDataMap, keyHandler.getKey()), keyHandler.getMethods());
					System.out.println(statDataMap);
					fixDailyStatsMap(statDataMap);
					System.out.println(statDataMap);
//				}
			} else {
				//TODO fix CollectionStatsGatherer
			}
*/
//TODO			statsCollNew.insert(new BasicDBObject(statDataMap));
//		}
//TODO
/*
		//if temp collection already exists delete it
		enkive.getCollection("statisticsTEMP").drop();
		//insert all of the old stats collection into it
		enkive.getCollection("statatisticsTEMP").insert(statsCollOld.find().toArray());
		//overwrite the old stats collection with the migrated one
		statsCollNew.rename(statsCollOld.getName(), true);
		//delete temp collection
		enkive.getCollection("statisticsTEMP").drop();
*/		return true;
	}
	
	public static void main(String args[]){
		try {
			(new StatsCollectionMigration(new Mongo())).migrate(null);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DBMigrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
