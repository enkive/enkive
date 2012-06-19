package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.MongoConstants.MONGO_LENGTH;
import static com.linuxbox.enkive.statistics.MongoConstants.MONGO_UPLOAD_DATE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_ATTACH;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_ATTACH;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;
import static com.linuxbox.enkive.statistics.StatsConstants.THIRTY_DAYS;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
public class StatsMongoAttachmentsGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering.StatsMongoAttachmentsGatherer");
	protected Mongo m;
	protected DB db;
	protected Date lower, upper;// uploadDate
	protected String collectionName;

	public StatsMongoAttachmentsGatherer(Mongo m, String dbName, String coll, String serviceName, String schedule) {
		super(serviceName, schedule);
		this.m = m;
		db = m.getDB(dbName);
		collectionName = coll + ".files";
	}
	
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

	protected Map<String, Set<String>> keyBuilder(){
		Map<String, Set<String>> keys = new HashMap<String, Set<String>>();
		keys.put(STAT_NAME, null);
		keys.put(STAT_AVG_ATTACH, makeCreator(GRAIN_AVG));
		keys.put(STAT_MAX_ATTACH, makeCreator(GRAIN_MAX));
		return keys;
	}
	
	private Map<String, Object> makeDateQuery() {
		Map<String, Object> dateQuery = createMap();
		dateQuery.put("$gte", lower);
		dateQuery.put("$lt", upper);
		Map<String, Object> query = createMap();
		query.put(MONGO_UPLOAD_DATE, dateQuery);
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
				long temp = ((Long) cursor.next().get(MONGO_LENGTH)).longValue();
				total += temp;
			}
			avgAttach = (double) total / count;
		} else {
			avgAttach = -1;
			LOGGER.warn("No attachments found between " + lower + " & " + upper);
		}
		return avgAttach;
	}

	public long getMaxAttachSize() {
		DBCollection coll = db.getCollection(collectionName);
		DBCursor cursor = coll.find();
		long max = -1;
		if (cursor.hasNext()) {
			while (cursor.hasNext()) {
				long temp = ((Long) cursor.next().get(MONGO_LENGTH)).longValue();
				if (temp > max) {
					max = temp;
				}
			}
		} else {
			LOGGER.warn("Empty Collection used in getMaxAttachSize()");
		}
		return max;
	}

	//TODO: variable dates
	public Map<String, Object> getStatistics() {
		long currTime = System.currentTimeMillis();

		// default sets dates to previous thirty days
		setUpper(new Date(currTime));
		setLower(new Date(currTime - THIRTY_DAYS));

		Map<String, Object> stats = new HashMap<String, Object>();
		double avg = getAvgAttachSize();
		long max = getMaxAttachSize();
		
		if(avg <= -1 || max <= -1){
			return null;
		}
		
		stats.put(STAT_AVG_ATTACH, avg);
		stats.put(STAT_MAX_ATTACH, max);
		stats.put(STAT_TIME_STAMP, System.currentTimeMillis());
		return stats;
	}

	public static void main(String args[]) throws UnknownHostException,
			MongoException {
		StatsMongoAttachmentsGatherer attachProps = new StatsMongoAttachmentsGatherer(
				new Mongo(), "enkive", "fs", "name", "cron");
		System.out.println(attachProps.getStatistics());
		String[] keys = { STAT_TYPE, STAT_NAME, STAT_DATA_SIZE, STAT_AVG_ATTACH };
		System.out.println(attachProps.getStatistics(keys));
	}

}
