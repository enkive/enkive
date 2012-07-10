package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_ATTACH;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_ATTACH;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_LENGTH;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_UPLOAD_DATE;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

// NOAH: please turn your comments of variables and methods within a class to JavaDoc comments. 
//They start with /** and end with */. 

// To use this class the workflow appears to be:
// Create instance of new StatsMongoAttachmentGatherer
// setUpper(), setLower()
// Then call getMax... and getAvg..., which take no arguments
// I'd like to see the ability to call getMax and getAvg with date arguments 
//which would make this class a little more flexible.

/*
 * FIXES:
 * changed names of date variables
 * added functionality to getAvg & getMax to take a date range
 */
public class StatsMongoAttachmentsGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering.StatsMongoAttachmentsGatherer");
	protected String collectionName;
	protected DB db;
	protected Date lowerDate, upperDate;// uploadDate of attachment object
	protected Mongo m;
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

	// for testing
	public StatsMongoAttachmentsGatherer(Mongo m, String dbName, String coll,
			String serviceName, String schedule, boolean resetDates) {
		super(serviceName, schedule);
		this.m = m;
		db = m.getDB(dbName);
		collectionName = coll + ".files";
		this.resetDates = resetDates;
	}

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
			avgAttach = -1;
			LOGGER.warn("getAvgAttachSize()-No attachments between " + lowerUploadDate
					+ " & " + upperUploadDate);
		}
		return avgAttach;
	}
	
	//avg attach size total
	public double getAvgAttachSize() {
		return getAvgAttachSize(getUpperDate(), getLowerDate());
	}



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
		}
		return max;
	}
	
	public long getMaxAttachSize() {
		return getMaxAttachSize(getUpperDate(), getLowerDate());
	}

	@Override
	public Map<String, Object> getStatistics() {
		long currTime = System.currentTimeMillis();
		// default sets dates to previous hour
		if (resetDates) {
			setUpperDate(new Date(currTime));
			setLowerDate(new Date(currTime - 3600000));
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

	private Map<String, Object> makeDateQuery() {
		return makeDateQuery(getLowerDate(), getUpperDate());
	}
	
	public void setLowerDate(Date lower) {
		this.lowerDate = lower;
	}

	public void setUpperDate(Date upper) {
		this.upperDate = upper;
	}
}
