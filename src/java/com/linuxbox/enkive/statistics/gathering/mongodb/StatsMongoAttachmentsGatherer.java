package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_NUM;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_ATTACH;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_ATTACH;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_LENGTH;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_UPLOAD_DATE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_HOUR;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.VarsMaker;
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
			String serviceName, String humanName, String schedule) {
		super(serviceName, humanName, schedule);
		this.m = m;
		this.db = m.getDB(dbName);
		this.attachmentsColl = db.getCollection(attachmentsColl + ".files");
	}

	public RawStats getStatistics(Date startDate, Date endDate) {
		if (upperDate == null) {
			LOGGER.warn("upper == null current time used");
			setUpperDate(new Date(currTime));
		}
		if (lowerDate == null) {
			LOGGER.warn("lower == null beginning of time used");
			setLowerDate(new Date(0L));
		}
		Map<String, Object> stats = new HashMap<String, Object>();
		double avg = getAvgAttachSize();
		long max = getMaxAttachSize();

		if (avg <= -1 || max <= -1) {
			return null;
		}
		stats.put(STAT_AVG_ATTACH, avg);
		stats.put(STAT_MAX_ATTACH, max);
		
		RawStats result = new RawStats();
		result.setStatsMap(stats);
		result.setTimestamp(new Date());
		return result;
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
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
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
		innerNumAttach.put(GRAIN_AVG, dataCursor.count());
		
		Map<String,Object> innerAttachSz = new HashMap<String,Object>();
		
		long avgAttSz = 0;
		if(dataCursor.count() != 0){
			avgAttSz = dataByteSz/dataCursor.count();
		}
		
		innerAttachSz.put(GRAIN_AVG, avgAttSz);
		
		Map<String, Object> dateMap = new HashMap<String, Object>();
		dateMap.put(GRAIN_MIN, startDate);
		dateMap.put(GRAIN_MAX, endDate);
		
		resultMap.put(STAT_ATTACH_SIZE, innerAttachSz);
		resultMap.put(STAT_ATTACH_NUM, innerNumAttach);
		resultMap.put(STAT_TIMESTAMP, dateMap);
		resultMap.put(GRAIN_TYPE, GRAIN_HOUR);
		resultMap.put(STAT_GATHERER_NAME, "AttachmentStatsService");
		
		RawStats resultStats = new RawStats();
		
		return result;
	}

	/**
	 * creates the query object with which to query the database
	 * @param upperUploadDate - upper date in range (less than)
	 * @param lowerUploadDate - lower date in range (greater than or equal to)
	 * @return
	 */
	private Map<String, Object> makeDateQuery(Date lowerUploadDate, Date upperUploadDate) {
		Map<String, Object> dateQuery = VarsMaker.createMap();
		dateQuery.put("$gte", lowerUploadDate);
		dateQuery.put("$lt", upperUploadDate);
		Map<String, Object> query = VarsMaker.createMap();
		query.put(MONGO_UPLOAD_DATE, dateQuery);
		return query;
	}
	
	public Date getLowerDate() {
		return lowerDate;
	}
	
	public Date getUpperDate() {
		return upperDate;
	}

	/**
	 * @return the query object cooresponding to two dates previously set by this class's
	 * date setters
	 */
	private Map<String, Object> makeDateQuery() {
		return makeDateQuery(getLowerDate(), getUpperDate());
	}
	
	public void setLowerDate(Date lower) {
		this.lowerDate = lower;
	}

	public void setUpperDate(Date upper) {
		this.upperDate = upper;
	}
	
	public void setInterval(long interval){
		this.interval = interval;
	}
}
