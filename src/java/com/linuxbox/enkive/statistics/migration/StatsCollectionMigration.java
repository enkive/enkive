package com.linuxbox.enkive.statistics.migration;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.*;
import static com.linuxbox.enkive.statistics.StatsConstants.*;
import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.retrieval.StatsQuery;
import com.linuxbox.enkive.statistics.services.retrieval.mongodb.MongoStatsQuery;
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
public class StatsCollectionMigration extends DBMigration {
	public StatsCollectionMigration(Mongo m, StatsClient client)
			throws DBMigrationException {
		super(null, 1, 12);
		this.client = client;
		this.m = m;
		this.enkive = m.getDB("enkive");
	}
	
	public StatsCollectionMigration(DBMigrator migrator, int fromVersion,
			int toVersion, Mongo m, StatsClient client)
			throws DBMigrationException {
		super(migrator, fromVersion, toVersion);
		this.client = client;
		this.m = m;
		this.enkive = m.getDB("enkive");
	}	
	
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.migration");
	StatsClient client;
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
	private void fixDailyData(Map<String, Object> statsData,
			Collection<String> methods) {
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

	
	
	@Override
	public boolean migrate(DBInfo db) throws DBMigrationException {
		if (enkive.collectionExists("statistics")) {
			this.statsCollOld = enkive.getCollection("statistics");
		} else {
			// TODO fail
			LOGGER.error("could not find statistics collection");
		}

		enkive.getCollection("statisticsV1_2_1").drop();
		this.statsCollNew = enkive.getCollection("statisticsV1_2_1");

		Integer[] validGrainTypes = {null, CONSOLIDATION_HOUR, CONSOLIDATION_DAY};
		DBObject goodEntriesQuery = new BasicDBObject(CONSOLIDATION_TYPE, new BasicDBObject("$in", validGrainTypes));
		
		//insert fixed data into the new collection one at a time
		for(DBObject statData: statsCollOld.find(goodEntriesQuery, new BasicDBObject("_id", false)).toArray()){
			Map<String, Object> statDataMap = statData.toMap();
			String gathererName = (String)statDataMap.get(STAT_GATHERER_NAME);
			GathererAttributes attributes = client.getAttributes(gathererName);
			if(gathererName.equals("CollectionStatsGatherer")){
				for(ConsolidationKeyHandler keyHandler: attributes.getKeys()){
					fixDailyData(getData(statDataMap, keyHandler.getKey()), keyHandler.getMethods());
				}
			} else {
				//TODO fix CollectionStatsGatherer
			}
			statsCollNew.insert(new BasicDBObject(statDataMap));
		}
		
		//statsCollNew.insert(statsCollOld.find(,new BasicDBObject("_id", false)).toArray());
/*
		// clear out bad weekly data
		statsCollNew.remove(new BasicDBObject(CONSOLIDATION_TYPE,
				CONSOLIDATION_WEEK));

		// clear out bad monthly data
		statsCollNew.remove(new BasicDBObject(CONSOLIDATION_TYPE,
				CONSOLIDATION_MONTH));

		StatsQuery query = null;

		// get & fix daily data for creation
		for (GathererAttributes attributes : client.getAttributes()) {
			query = new MongoStatsQuery(attributes.getName(), CONSOLIDATION_DAY);
			if (!attributes.getName().equals("CollectionStatsGatherer")) {
				for (ConsolidationKeyHandler keyHandler : attributes.getKeys()) {
					if (keyHandler.getMethods() != null) {
						// get the data
						for (Map<String, Object> statsData : client
								.queryStatistics(query)) {
							// fix data
							fixDailyData(
									getData(statsData, keyHandler.getKey()),
									keyHandler.getMethods());
						}
					}
				}
			} else {
				// TODO fix collection stats gatherer
			}
		}
		
		// TODO
		// consolidate weekly from daily
		// consolidate monthly from weekly

		// drop the old statsCollectoin
		//statsCollOld.drop();
		// rename the new one 'statistics'
		statsCollNew.rename(statsCollOld.getName(), true); //TODO the true specifies the drop target being removed...
		*/
		return false;
	}
	
	public static void main(String args[]) throws UnknownHostException, DBMigrationException{
		Mongo m = new Mongo();
		DB enkive = m.getDB("enkive");
		DBCollection statsCollOld = enkive.getCollection("statistics");
		DBCollection statsCollNew;
		if (enkive.collectionExists("statistics")) {
			statsCollOld = enkive.getCollection("statistics");
		} else {
			// TODO fail
			LOGGER.error("could not find statistics collection");
		}

		enkive.getCollection("statisticsV1_2_1").drop();
		statsCollNew = enkive.getCollection("statisticsV1_2_1");

		// insert all the old data into the new statistics collection
		Integer[] validGrainTypes = {null, CONSOLIDATION_HOUR, CONSOLIDATION_DAY};
		DBObject oldStatsQuery = new BasicDBObject("$in", validGrainTypes);
		DBObject goodEntriesQuery = new BasicDBObject(CONSOLIDATION_TYPE, oldStatsQuery);
		for(DBObject statData: statsCollOld.find(goodEntriesQuery, new BasicDBObject("_id", false)).toArray()){
			System.out.println("statData: " + statData.toString());
		}
	}
	
}
