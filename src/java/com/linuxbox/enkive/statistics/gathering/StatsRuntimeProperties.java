package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_PROCESSORS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MEMORY;

import java.util.Map;

import org.json.JSONObject;

import com.mongodb.BasicDBObject;

public class StatsRuntimeProperties implements StatsGatherer {

	public BasicDBObject getStats() {
		BasicDBObject stats = new BasicDBObject();
		Runtime runtime = Runtime.getRuntime();
		Long maxMemory = new Long(runtime.maxMemory());
		Long freeMemory = new Long(runtime.freeMemory());
		Long totalMemory = new Long(runtime.totalMemory());
		stats.put(STAT_MAX_MEMORY, maxMemory);
		stats.put(STAT_FREE_MEMORY, freeMemory);
		stats.put(STAT_TOTAL_MEMORY, totalMemory);
		stats.put(STAT_PROCESSORS, runtime.availableProcessors());
		stats.put(STAT_TIME_STAMP, System.currentTimeMillis());
		return stats;
	}

	public JSONObject getStatisticsJSON() {
		BasicDBObject stats = getStats();
		JSONObject result = new JSONObject(stats);
		return result;
	}

	public JSONObject getStatisticsJSON(Map<String, String> map) {
		// TODO: Implement
		return getStatisticsJSON();
	}
}
