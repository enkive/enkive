package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.ARCHIVE_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.*;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoMessageStatisticsService implements StatsGatherer {

	protected Mongo m = null;
	protected DB messageDb;
	protected DBCollection messageColl;

	public MongoMessageStatisticsService(Mongo m, String dbName, String collName) {
		this.m = m;
		messageDb = m.getDB(dbName);
		messageColl = messageDb.getCollection(collName);
	}
	
	@Override
	public JSONObject getStatisticsJSON() throws JSONException {
		JSONObject result = new JSONObject();
		result.put(STAT_TIME_STAMP, System.currentTimeMillis());
		result.put(ARCHIVE_SIZE, messageColl.count());
		return result;
	}

	public JSONObject getStatisticsJSON(Map<String,String> map) throws JSONException{
		//TODO: Implement
		return getStatisticsJSON();
	}
}
