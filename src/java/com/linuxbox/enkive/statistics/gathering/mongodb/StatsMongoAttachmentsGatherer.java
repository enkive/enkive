package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_ATTACH;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_ATTACH;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_LENGTH;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_UPLOAD_DATE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

public class StatsMongoAttachmentsGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering.StatsMongoAttachmentsGatherer");
	protected String collectionName;
	protected DB db;
	protected Date lowerDate, upperDate;// uploadDate of attachment object
	protected Mongo m;
	protected long interval = 60 * 60 * 1000; //default interval is hour
	
	// NOAH:TODO I think we need some help w/ the logic of resetDates. Who controls
	// whether it's true/false and why? Who is supposed to check it and then set
	// dates if it is true?
	//
	// if resetDates is false you must manually reset the upper & lower dates
	private boolean resetDates;

	public StatsMongoAttachmentsGatherer(Mongo m, String dbName, String coll,
			String serviceName, String schedule) {
		super(serviceName, schedule);
		this.m = m;
		db = m.getDB(dbName);
		collectionName = coll + ".files";
		resetDates = true;
	}
	
	/**
	 * this constructor should only be used for testing
	 * @param m
	 * @param dbName
	 * @param coll
	 * @param serviceName
	 * @param schedule
	 * @param resetDates
	 * @throws GathererException 
	 */
	public StatsMongoAttachmentsGatherer(Mongo m, String dbName, String coll,
			String serviceName, String schedule, boolean resetDates, List<String> keys) throws GathererException {
		super(serviceName, schedule, keys);
		this.m = m;
		db = m.getDB(dbName);
		collectionName = coll + ".files";
		this.resetDates = resetDates;
	}

	/**
	 * @param upperUploadDate - upper date in range (less than)
	 * @param lowerUploadDate - lower date in range (greater than or equal to)
	 * @return the average attachment size between two dates
	 */
	public double getAvgAttachSize(Date upperUploadDate, Date lowerUploadDate) {
		DBCollection coll = db.getCollection(collectionName);
		DBCursor cursor = coll.find(new BasicDBObject(makeDateQuery()));
		double avgAttach;
		if (cursor.hasNext()) {
			int count = cursor.size();
			long total = 0;
			while (cursor.hasNext()) {
				long temp = ((Long) cursor.next().get(MONGO_LENGTH))
						.longValue();
				total += temp;
			}
			avgAttach = (double) total / count;
		} else {
			avgAttach = 0;
			LOGGER.warn("getAvgAttachSize()-No attachments between " + lowerUploadDate
					+ " & " + upperUploadDate);
		}
		return avgAttach;
	}
	
	/**
	 * @return the average attachment size between two dates previously set by this class's
	 * date setters
	 */
	public double getAvgAttachSize() {
		return getAvgAttachSize(getUpperDate(), getLowerDate());
	}

	/**
	 * @param upperUploadDate - upper date in range (less than)
	 * @param lowerUploadDate - lower date in range (greater than or equal to)
	 * @return the max attachment size between two dates
	 */
	public long getMaxAttachSize(Date lowerDate, Date upperDate) {
		DBCollection coll = db.getCollection(collectionName);
		DBCursor cursor = coll.find(new BasicDBObject(makeDateQuery()));
		long max = -1;
		if (cursor.hasNext()) {
			while (cursor.hasNext()) {
				long temp = ((Long) cursor.next().get(MONGO_LENGTH))
						.longValue();
				if (temp > max) {
					max = temp;
				}
			}
		} else {
			LOGGER.warn("getMaxAttachSize()-No attachments between " + lowerDate
					+ " & " + upperDate);
			max = 0;
		}
		return max;
	}
	
	/**
	 * @return the max attachment size between two dates previously set by this class's
	 * date setters
	 */
	public long getMaxAttachSize() {
		return getMaxAttachSize(getUpperDate(), getLowerDate());
	}

	@Override
	public Map<String, Object> getStatistics() {
		long currTime = System.currentTimeMillis();
		if (resetDates) {
			setUpperDate(new Date(currTime));
			setLowerDate(new Date(currTime - interval));
		}
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
		stats.put(STAT_TIME_STAMP, new Date());
		return stats;
	}

	/**
	 * creates the query object with which to query the database
	 * @param upperUploadDate - upper date in range (less than)
	 * @param lowerUploadDate - lower date in range (greater than or equal to)
	 * @return
	 */
	private Map<String, Object> makeDateQuery(Date lowerUploadDate, Date upperUploadDate) {
		Map<String, Object> dateQuery = createMap();
		dateQuery.put("$gte", lowerUploadDate);
		dateQuery.put("$lt", upperUploadDate);
		Map<String, Object> query = createMap();
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
