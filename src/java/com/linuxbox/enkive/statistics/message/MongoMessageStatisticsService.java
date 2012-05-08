package com.linuxbox.enkive.statistics.message;

import static com.linuxbox.enkive.statistics.StatsConstants.ARCHIVE_SIZE;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.statistics.StatsService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoMessageStatisticsService implements StatsService {

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
		result.append(ARCHIVE_SIZE, messageColl.count());
		return result;
	}

	public JSONObject getStatisticsJSON(Map<String,String> map) throws JSONException{
		//TODO: Implement
		return getStatisticsJSON();
	}
}
