package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_NUM;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_AVG;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_LENGTH;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_UPLOAD_DATE;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class StatsMongoAttachmentsGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering.StatsMongoAttachmentsGatherer");
	protected Mongo m;
	protected DB db;
	protected DBCollection attachmentsColl;
	
	public StatsMongoAttachmentsGatherer(Mongo m, String dbName, String attachmentsColl,
			String serviceName, String humanName, String schedule, List<String> keys) throws GathererException {
		super(serviceName, humanName, schedule, keys);
		this.m = m;
		this.db = m.getDB(dbName);
		this.attachmentsColl = db.getCollection(attachmentsColl + ".files");
	}
	
	@Override
	public RawStats getStatistics() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		Date endDate = cal.getTime();
		cal.add(Calendar.HOUR, -1);
		Date startDate = cal.getTime();
		
		return getStatistics(startDate, endDate);
	}
	
	public RawStats getStatistics(Date startDate, Date endDate) {
		Map<String, Object> intervalMap = new HashMap<String, Object>();
		Map<String, Object> query = new HashMap<String, Object>();
		Map<String, Object> innerQuery = new HashMap<String, Object>();
		innerQuery.put("$gte", startDate);
		innerQuery.put("$lt", endDate);
		query.put(MONGO_UPLOAD_DATE, innerQuery);
		long dataByteSz = 0;
		DBCursor dataCursor = attachmentsColl.find(new BasicDBObject(query));
		
		for(DBObject obj: dataCursor){
			dataByteSz+=(Long)(obj.get(MONGO_LENGTH));
		}
		Map<String,Object> innerNumAttach = new HashMap<String,Object>();
		innerNumAttach.put(CONSOLIDATION_AVG, dataCursor.count());
		
		long avgAttSz = 0;
		if(dataCursor.count() != 0){
			avgAttSz = dataByteSz/dataCursor.count();
		}
		
		intervalMap.put(STAT_ATTACH_SIZE, avgAttSz);
		intervalMap.put(STAT_ATTACH_NUM, dataCursor.count());
		intervalMap.put(STAT_GATHERER_NAME, "AttachmentStatsService");
		
		RawStats resultStats = new RawStats(intervalMap, null, startDate, endDate);
		
		return resultStats;
	}
}
