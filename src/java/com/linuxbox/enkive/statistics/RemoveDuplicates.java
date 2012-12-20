package com.linuxbox.enkive.statistics;

import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;
import com.linuxbox.enkive.statistics.services.storage.mongodb.MongoStatsStorageService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_DB;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_COLLECTION;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_TYPE;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_WEEK;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MONTH;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_ID;


public class RemoveDuplicates {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.RemoveDuplicates");
	private DBCollection statistics;
	private StatsStorageService storageService;
	private Calendar startTimestamp = Calendar.getInstance();
	private Calendar endTimestamp = Calendar.getInstance();
	
	public RemoveDuplicates(Mongo mongo, StatsStorageService storageService){
		this.statistics = mongo.getDB(STAT_STORAGE_DB).getCollection(STAT_STORAGE_COLLECTION);
		this.storageService = storageService;
		setStartTimestamp();
	}
	
	private void setStartTimestamp(){
		this.startTimestamp.set(Calendar.YEAR, 2012);
		this.startTimestamp.set(Calendar.MONTH, Calendar.AUGUST);
		this.startTimestamp.set(Calendar.DAY_OF_MONTH, 1);
		this.startTimestamp.set(Calendar.HOUR_OF_DAY, 0);
		this.startTimestamp.set(Calendar.MINUTE, 0);
		this.startTimestamp.set(Calendar.SECOND, 0);
		this.startTimestamp.set(Calendar.MILLISECOND, 0);
		System.out.println("the start date is: " + startTimestamp.getTime());
		System.out.println("the end date is: " + endTimestamp.getTime());
	}
	
	private void removeByID(Object id){
		if(id instanceof ObjectId){
			Map<String,ObjectId> objectToRemove = new HashMap<String, ObjectId>();
			objectToRemove.put(MONGO_ID, (ObjectId) id);
			statistics.remove(new BasicDBObject(objectToRemove));
		} else {
			LOGGER.error("Object id is the wrong type");
		}
	}
	
	@SuppressWarnings("unchecked")
	public void removeDuplicateData(){
		Calendar queryStartTime = Calendar.getInstance();
		queryStartTime.setTime(startTimestamp.getTime());
		queryStartTime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		
		Calendar queryEndTime = Calendar.getInstance();
		queryEndTime.setTime(queryStartTime.getTime());
		
		DBObject query = new BasicDBObject();
		String timestampMin = STAT_TIMESTAMP + "." + CONSOLIDATION_MIN;
		String timestampMax = STAT_TIMESTAMP + "." + CONSOLIDATION_MAX;
		
		while(queryStartTime.before(endTimestamp)){
			queryEndTime.add(Calendar.DATE, 7);
			query.put(timestampMin, queryStartTime.getTime());
			query.put(timestampMax, queryEndTime.getTime());
			
			query.put(CONSOLIDATION_TYPE, CONSOLIDATION_WEEK);
			
			DBCursor cursor = statistics.find(query);
			System.out.println("cursor count: " + cursor.count());
			if(cursor.count() > 0){		
				List<Map<String,Object>> dataToKeep  = new LinkedList<Map<String,Object>>();
				
				boolean shouldBeKept;
				for(DBObject statDataObj: cursor){
					Map<String,Object> statData = statDataObj.toMap();
					shouldBeKept = true;
					String gathererName = statData.get(STAT_GATHERER_NAME).toString();
					
					//check to see if already have that gatherer's data
					for(Map<String,Object> uniqueStatData: dataToKeep){
						String uniqueStatName = uniqueStatData.get(STAT_GATHERER_NAME).toString();
						if(gathererName.equals(uniqueStatName)){
							shouldBeKept = false;
							break;
						}
					}
					
					//Add unique data to dataToKeep
					if(shouldBeKept){
						Map<String,Object> statDataToKeep = new HashMap<String,Object>(statData);
						statDataToKeep.remove(MONGO_ID);
						dataToKeep.add(statDataToKeep);
					} else {
						removeByID(statData.get(MONGO_ID));
					}
				}				
			}			
			queryStartTime.setTime(queryEndTime.getTime());
		}		
		
		//query over month
		//...
	}
	
	public static void main(String args[]){
		Mongo mongo = null;
		try {
			mongo = new Mongo();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
		StatsStorageService storageService = new MongoStatsStorageService(mongo);
		RemoveDuplicates foo = new RemoveDuplicates(mongo, storageService);
		foo.removeDuplicateData();
		
		
	}
}
