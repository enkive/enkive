package com.linuxbox.enkive.statistics;

import static com.linuxbox.enkive.statistics.StatisticsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatisticsConstants.STAT_MAX_MEMORY;
import static com.linuxbox.enkive.statistics.StatisticsConstants.STAT_PROCESSORS;
import static com.linuxbox.enkive.statistics.StatisticsConstants.STAT_TOTAL_MEMORY;

import org.json.JSONObject;

import com.mongodb.BasicDBObject;

public class StatisticsRuntimeProperties implements StatisticsService{

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
}
