package com.linuxbox.enkive.statistics;

import static com.linuxbox.enkive.statistics.StatsConstants.THIRTY_DAYS;

import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

public class StatsMongoAttachments implements StatsService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");
	protected Mongo m;
	protected DB db;
	protected Date lower, upper;//uploadDate
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

	public StatsMongoAttachments(Mongo m, String dbName, String coll) {
		this.m = m;
		db = m.getDB(dbName);
		collectionName = coll + ".files";
	}

	private BasicDBObject makeDateQuery() {
		BasicDBObject dateQuery = new BasicDBObject();
		dateQuery.put("$gte", lower);
		dateQuery.put("$lte", upper);
		BasicDBObject query = new BasicDBObject();
		query.put("uploadDate", dateQuery);
		return query;
	}

	public BasicDBObject getAvgAttachSize() {
		DBCollection coll = db.getCollection(collectionName);
		DBCursor cursor = coll.find(makeDateQuery());
		double avgAttach;
		if (cursor.hasNext()) {
			int count = cursor.size();
			long total = 0;
			while (cursor.hasNext()) {
				long temp = ((Long)cursor.next().get("length")).longValue();
				total += temp;
			}
			avgAttach = (double) total / count;
		} else {
			avgAttach = -1;
			LOGGER.warn("Empty Collection used in getAvgAttachSize()");
		}
		return new BasicDBObject("AvgAttachSize", avgAttach);
	}

	public BasicDBObject getMaxAttachSize() {
		DBCollection coll = db.getCollection(collectionName);
		DBCursor cursor = coll.find();
		long max = -1;
		if (cursor.hasNext()) {
			while (cursor.hasNext()) {
				long temp = ((Long)cursor.next().get("length")).longValue();
				if(temp > max){
					max = temp;
				}
			}
		} else {
			LOGGER.warn("Empty Collection used in getMaxAttachSize()");
		}
		
		return new BasicDBObject("MaxAttachSize", max);
	}
	
	public JSONObject getStatisticsJSON() {
		long currTime = System.currentTimeMillis();
		 
		
		//default sets dates to previous thirty days
		setUpper(new Date(currTime));
		setLower(new Date(currTime-THIRTY_DAYS));
		
		BasicDBObject avgStat = getAvgAttachSize();
		BasicDBObject maxStat = getMaxAttachSize();
		BasicDBObject stats = new BasicDBObject();
		stats.put("avgStat", avgStat);
		stats.put("maxStat", maxStat);
		JSONObject result = new JSONObject(stats);
		return result;
	}
	
	//sets the date--remove setting these in constructor
	public JSONObject getStatisticsJSON(Map<String,String> map){
		//TODO: Implement
		return getStatisticsJSON();
	}
	
}
