package com.linuxbox.enkive.statistics.migration;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.*;

import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Migrates the data from 1.2 to 1.2.1 (cleans out duplicate entries and re-inserts the sums)
 * @author noah
 *
 */
public class StatsCollectionMigrator {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.migration");
	StatsClient client;
	Mongo m;
	DB enkive;
	DBCollection metaColl;
	DBCollection statsCollV1_2_0;
	DBCollection statsCollV1_2_1;
	String currentVersion;
	
	private boolean enkiveIsVersion1_2_1(){
		//TODO
		return false;
	}
	
	private Map<String,Object> getData(Map<String, Object> statsMap, List<String> path){
		for(String key: path){
			statsMap = (Map<String,Object>)statsMap.get(key);
		}
		return statsMap;
	}
	
	private void fixDailyData(Map<String, Object> statsData, Collection<String> methods){
		if(methods.contains(CONSOLIDATION_AVG)){
			
		}
	}
	
	public StatsCollectionMigrator(Mongo m, StatsClient client){
		this.client = client;
		this.m = m;
		this.enkive = m.getDB("enkive");
		
		if(enkive.collectionExists("enkive_meta")){
			metaColl = enkive.getCollection("enkive_meta");
			//currentVersion = metaColl.getCurrVersion()
			//if(currentVersion.equals("1.2.1")){
				//fail
			//}
		} else {
			LOGGER.info("meta-data collection does not exist. Assuming enkive installation is previous to 1.2.1");
		}
		
		//if(metaColl.isLocked()){
		//	fail
		//}
		
		if(enkive.collectionExists("statistics")){
			this.statsCollV1_2_0 = enkive.getCollection("statistics");
		} else {
			//TODO fail
			LOGGER.error("could not find statistics collection");
		}
		
		if(enkiveIsVersion1_2_1()){
			enkive.getCollection("statisticsV1_2_1").drop();
		}
		this.statsCollV1_2_1 = enkive.getCollection("statisticsV1_2_1");
		
		//insert all the old data into the new statistics collection
		statsCollV1_2_1.insert(statsCollV1_2_0.find().toArray());
		
		//clear out bad weekly data
		statsCollV1_2_1.remove(new BasicDBObject(CONSOLIDATION_TYPE, CONSOLIDATION_WEEK));
		
		//clear out bad monthly data
		statsCollV1_2_1.remove(new BasicDBObject(CONSOLIDATION_TYPE, CONSOLIDATION_MONTH));
		
		DBObject query = new BasicDBObject(CONSOLIDATION_TYPE, CONSOLIDATION_DAY);
		
		for(GathererAttributes attributes: client.getAttributes()){
			if(!attributes.getName().equals("CollectionStatsGatherer")){
				for(ConsolidationKeyHandler keyHandler: attributes.getKeys()){
					if(keyHandler.getMethods() != null){
						List<String> path = keyHandler.getKey();
						for(String mapKey: path){
							//TODO go down the path & get to the data
							//Fix it
						}
					}
				}
			} else {
				//TODO
			}
		}
//		DBObject query = new BasicDBObject();
		
		//fix the daily data
		//if we have the average for the daily then multiply it by 24 to get the sum
		//else if we have the max & min for the daily then average them
		//else we're toast
		
		//consolidate weekly from daily
		//consolidate monthly from weekly
	}
}
