package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FILE_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_COLLECTIONS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_INDEX;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_OBJS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsMongoDBGatherer extends AbstractGatherer {	
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");

	protected Mongo m;
	protected DB db;

	public StatsMongoDBGatherer(){
		try {
			this.m = new Mongo();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setSchedule("0/10 * * * * ?");
		db = m.getDB("enkive");
	}
	
	public StatsMongoDBGatherer(Mongo m, String dbName) {
		this.m = m;
		db = m.getDB(dbName);
		setSchedule("0/10 * * * * ?");
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("Date: " + new Date());
	}
	
	public BasicDBObject getStats() {
		BasicDBObject stats = new BasicDBObject();
		BasicDBObject temp = db.getStats();
		stats.put(STAT_TYPE, "database");
		stats.put(STAT_NAME, db.getName());
		stats.put(STAT_NUM_COLLECTIONS, temp.get("collections"));
		stats.put(STAT_NUM_OBJS, temp.get("objects"));
		stats.put(STAT_AVG_OBJ_SIZE, temp.get("avgObjSize"));
		stats.put(STAT_DATA_SIZE, temp.get("dataSize"));
		stats.put(STAT_TOTAL_SIZE, temp.get("storageSize"));
		stats.put(STAT_NUM_INDEX, temp.get("indexes"));
		stats.put(STAT_TOTAL_INDEX_SIZE, temp.get("indexSize"));
		stats.put(STAT_NUM_EXTENT, temp.get("numExtents"));
		stats.put(STAT_FILE_SIZE, temp.get("fileSize"));
		stats.put(STAT_TIME_STAMP, System.currentTimeMillis());
		return stats;
	}

	public Map<String, Object> getStatistics() {
//		attributes.incrementTime();
		return getStats();
	}

	public static void main(String args[]) throws UnknownHostException,
			MongoException {
		StatsMongoDBGatherer dbProps = new StatsMongoDBGatherer(new Mongo(),
				"enkive");
		System.out.println(dbProps.getStatistics());
		String[] keys = { STAT_TYPE, STAT_NAME, STAT_NUM_OBJS, STAT_FILE_SIZE };
		System.out.println(dbProps.getStatistics(keys));
	}
}
