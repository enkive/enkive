package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;
import static com.linuxbox.enkive.statistics.StatsConstants.THIRTY_DAYS;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static com.linuxbox.enkive.statistics.StatsConstants.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsMongoAttachmentsGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");
	protected Mongo m;
	protected DB db;
	protected Date lower, upper;// uploadDate
	protected String collectionName;

	public Date getLower() {
		return lower;
	}

	public Date getUpper() {
		return upper;
	}

	public void setLower(Date lower) {
		this.lower = lower;
	}

	public void setUpper(Date upper) {
		this.upper = upper;
	}

	public StatsMongoAttachmentsGatherer(Mongo m, String dbName, String coll) {
		this.m = m;
		db = m.getDB(dbName);
		collectionName = coll + ".files";
//		setAttributes();
	}

	private Map<String, Object> makeDateQuery() {
		Map<String, Object> dateQuery = createMap();
		dateQuery.put("$gte", lower);
		dateQuery.put("$lte", upper);
		Map<String, Object> query = createMap();
		query.put("uploadDate", dateQuery);
		return query;
	}

	public double getAvgAttachSize() {
		DBCollection coll = db.getCollection(collectionName);
		DBCursor cursor = coll.find(new BasicDBObject(makeDateQuery()));
		double avgAttach;
		if (cursor.hasNext()) {
			int count = cursor.size();
			long total = 0;
			while (cursor.hasNext()) {
				long temp = ((Long) cursor.next().get("length")).longValue();
				total += temp;
			}
			avgAttach = (double) total / count;
		} else {
			avgAttach = -1;
			LOGGER.warn("Empty Collection used in getAvgAttachSize()");
		}
		return avgAttach;
	}

	public long getMaxAttachSize() {
		DBCollection coll = db.getCollection(collectionName);
		DBCursor cursor = coll.find();
		long max = -1;
		if (cursor.hasNext()) {
			while (cursor.hasNext()) {
				long temp = ((Long) cursor.next().get("length")).longValue();
				if (temp > max) {
					max = temp;
				}
			}
		} else {
			LOGGER.warn("Empty Collection used in getMaxAttachSize()");
		}
		return max;
	}

	public Map<String, Object> getStatistics() {
		long currTime = System.currentTimeMillis();

		// default sets dates to previous thirty days
		setUpper(new Date(currTime));
		setLower(new Date(currTime - THIRTY_DAYS));

		Map<String, Object> stats = new HashMap<String, Object>();
		stats.put(STAT_AVG_ATTACH, getAvgAttachSize());
		stats.put(STAT_MAX_ATTACH, getMaxAttachSize());
		stats.put(STAT_TIME_STAMP, System.currentTimeMillis());
//		attributes.incrementTime();
		return stats;
	}

	public static void main(String args[]) throws UnknownHostException,
			MongoException {
		StatsMongoAttachmentsGatherer attachProps = new StatsMongoAttachmentsGatherer(
				new Mongo(), "enkive", "fs");
		System.out.println(attachProps.getStatistics());
		String[] keys = { STAT_TYPE, STAT_NAME, STAT_DATA_SIZE, STAT_AVG_ATTACH };
		System.out.println(attachProps.getStatistics(keys));
	}

}
