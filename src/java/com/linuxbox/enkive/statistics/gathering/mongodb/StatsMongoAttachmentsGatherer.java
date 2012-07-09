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

// NOAH: please turn your comments of variables and methods within a class to JavaDoc comments. They start with /** and end with */. 

public class StatsMongoAttachmentsGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering.StatsMongoAttachmentsGatherer");
	protected String collectionName;
	protected DB db;
	// NOAH: lower and upper are not very self-describing; rename along w/
	// setters.
	protected Date lower, upper;// uploadDate
	protected Mongo m;
	// NOAH: I think we need some help w/ the logic of resetDates. Who controls
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

	public double getAvgAttachSize() {
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
			LOGGER.warn("getAvgAttachSize()-No attachments between " + lower
					+ " & " + upper);
		}
		return avgAttach;
	}

	public Date getLower() {
		return lower;
	}

	public long getMaxAttachSize() {
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
			LOGGER.warn("getMaxAttachSize()-No attachments between " + lower
					+ " & " + upper);
		}
		return max;
	}

	// TODO: variable dates
	@Override
	public Map<String, Object> getStatistics() {
		long currTime = System.currentTimeMillis();
		// default sets dates to previous hour
		if (resetDates) {
			setUpper(new Date(currTime));
			setLower(new Date(currTime - 3600000));
		}
		if (upper == null) {
			LOGGER.warn("upper == null current time used");
			setUpper(new Date(currTime));
		}
		if (lower == null) {
			LOGGER.warn("lower == null beginning of time used");
			setLower(new Date(0L));
		}
		Map<String, Object> stats = new HashMap<String, Object>();
		double avg = getAvgAttachSize();
		long max = getMaxAttachSize();

		if (avg <= -1 || max <= -1) {
			return null;
		}
		stats.put(STAT_AVG_ATTACH, avg);
		stats.put(STAT_MAX_ATTACH, max);
		stats.put(STAT_TIME_STAMP, new Date(System.currentTimeMillis()));
		return stats;
	}

	public Date getUpper() {
		return upper;
	}

	/*
	 * @Override protected List<KeyDef> keyBuilder() { List<KeyDef> keys = new
	 * LinkedList<KeyDef>(); keys.add(new KeyDef(STAT_AVG_ATTACH + ":" +
	 * GRAIN_AVG)); keys.add(new KeyDef(STAT_MAX_ATTACH + ":" + GRAIN_MAX + ","
	 * + GRAIN_MIN)); return keys; }
	 */
	private Map<String, Object> makeDateQuery() {
		Map<String, Object> dateQuery = createMap();
		dateQuery.put("$gte", lower);
		dateQuery.put("$lt", upper);
		Map<String, Object> query = createMap();
		query.put(MONGO_UPLOAD_DATE, dateQuery);
		return query;
	}

	public void setLower(Date lower) {
		this.lower = lower;
	}

	public void setUpper(Date upper) {
		this.upper = upper;
	}
}
