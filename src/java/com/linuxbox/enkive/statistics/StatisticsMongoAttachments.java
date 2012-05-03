package com.linuxbox.enkive.statistics;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class StatisticsMongoAttachments implements StatisticsService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");
	protected Mongo m;
	protected DB db;
	protected Date lower, upper;

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

	public StatisticsMongoAttachments(Mongo m, String dbName) {
		this.m = m;
		db = m.getDB(dbName);
	}

	BasicDBObject makeDateQuery() {
		BasicDBObject dateQuery = new BasicDBObject();
		dateQuery.put("$gte", lower);
		dateQuery.put("$lte", upper);
		BasicDBObject query = new BasicDBObject();
		query.put("uploadDate", dateQuery);
		return query;
	}

	BasicDBObject getAvgAttachSize() {
		DBCollection coll = db.getCollection("fs.files");
		DBCursor cursor = coll.find(makeDateQuery());
		double avgAttach;
		// System.out.println("\nlower: " + lower + " " + lower.getTime());
		// System.out.println("upper: " + upper + " " + upper.getTime());
		// System.out.println("query: " + makeDateQuery() + "\n");
		if (cursor.hasNext()) {
			int count = cursor.size();
			long total = 0;
			while (cursor.hasNext()) {
				/*
				 * GridFSDBFile temp = new GridFSDBFile(); GridFSDBFile temp =
				 * cursor.next(); total += temp.getLength();
				 */
				DBObject temp = cursor.next();
				total += (Long) temp.get("length");
			}
			avgAttach = (1.0 * total) / count;
		} else {
			avgAttach = -1;
			LOGGER.warn("Empty Collection used in getAvgAttachSize(Date, Date)");
		}
		return new BasicDBObject("AvgAttachSize", avgAttach);
	}

	public JSONObject getStatisticsJSON() {
		BasicDBObject stats = getAvgAttachSize();
		JSONObject result = new JSONObject(stats);
		return result;
	}
}
