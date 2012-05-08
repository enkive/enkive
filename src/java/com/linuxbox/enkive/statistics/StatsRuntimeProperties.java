package com.linuxbox.enkive.statistics;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_PROCESSORS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MEMORY;

import java.util.Map;

import org.json.JSONObject;

import com.mongodb.BasicDBObject;

public class StatsRuntimeProperties implements StatsService{

	public BasicDBObject getStats() {
		BasicDBObject stats = new BasicDBObject();
		Runtime runtime = Runtime.getRuntime();
		stats.put(STAT_MAX_MEMORY, runtime.maxMemory());
		stats.put(STAT_FREE_MEMORY, runtime.freeMemory());
		stats.put(STAT_TOTAL_MEMORY, runtime.totalMemory());
		stats.put(STAT_PROCESSORS, runtime.availableProcessors());
		return stats;
	}
	
	public JSONObject getStatisticsJSON(){
		BasicDBObject stats = getStats();
		JSONObject result = new JSONObject(stats);
		return result;
	}
	
	public JSONObject getStatisticsJSON(Map<String,String> map){
		//TODO: Implement
		return getStatisticsJSON();
	}
}
