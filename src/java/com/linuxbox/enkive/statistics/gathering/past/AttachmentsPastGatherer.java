package com.linuxbox.enkive.statistics.gathering.past;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_NUM;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_ARCHIVE_SIZE;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_AVG;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_TYPE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_LENGTH;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_UPLOAD_DATE;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.linuxbox.enkive.statistics.services.StatsClient;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class AttachmentsPastGatherer extends PastGatherer{
	DBCollection attachmentsColl;
	
	public AttachmentsPastGatherer(Mongo m, String dbName, String attachmentsColl, String statisticsColl, String name, StatsClient client, int hrKeepTime, int dayKeepTime, int weekKeepTime, int monthKeepTime) {
		super(name, client, hrKeepTime, dayKeepTime, weekKeepTime, monthKeepTime);
		this.attachmentsColl = m.getDB(dbName).getCollection(attachmentsColl + ".files");
	}
	
	@PostConstruct
	public void init(){
//TODO		System.out.println("gathererName: " + gathererName);
		System.out.println("Start: " + new Date());
		consolidatePastHours();
		consolidatePastDays();
		consolidatePastWeeks();
		consolidatePastMonths();
		System.out.println("End: " + new Date());
	}
	
	protected Map<String, Object> getConsolidatedData(Date start, Date end, int grain){
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> query = new HashMap<String, Object>();
		Map<String, Object> innerQuery = new HashMap<String, Object>();
		innerQuery.put("$gte", start);
		innerQuery.put("$lt", end);
		query.put(MONGO_UPLOAD_DATE, innerQuery);
		long dataByteSz = 0;
		DBCursor dataCursor = attachmentsColl.find(new BasicDBObject(query));
		
		for(DBObject obj: dataCursor){
			dataByteSz+=(Long)(obj.get(MONGO_LENGTH));
		}
		Map<String,Object> innerNumAttach = new HashMap<String,Object>();
		innerNumAttach.put(CONSOLIDATION_AVG, dataCursor.count());
		
		Map<String,Object> innerAttachSz = new HashMap<String,Object>();
		
		long avgAttSz = 0;
		if(dataCursor.count() != 0){
			avgAttSz = dataByteSz/dataCursor.count();
		}
		
		innerAttachSz.put(CONSOLIDATION_AVG, avgAttSz);
		
		Map<String,Object> innerAttArchiveSize = new HashMap<String,Object>();
		innerAttArchiveSize.put(CONSOLIDATION_AVG, attachmentsColl.count());
		
		Map<String, Object> dateMap = new HashMap<String, Object>();
		dateMap.put(CONSOLIDATION_MIN, start);
		dateMap.put(CONSOLIDATION_MAX, end);
		
		result.put(STAT_ATTACH_SIZE, innerAttachSz);
		result.put(STAT_ATTACH_NUM, innerNumAttach);
		result.put(STAT_ATTACH_ARCHIVE_SIZE, innerAttArchiveSize);
		result.put(STAT_TIMESTAMP, dateMap);
		result.put(CONSOLIDATION_TYPE, grain);
		result.put(STAT_GATHERER_NAME, gathererName);
		
		return result;
	}
}
