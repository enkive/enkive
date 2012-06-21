package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_PROCESSORS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
public class StatsRuntimeGatherer extends AbstractGatherer {
	
	public StatsRuntimeGatherer(String serviceName, String schedule){
		super(serviceName, schedule);
	}
	
	protected Map<String, Set<String>> keyBuilder(){
		Map<String, Set<String>> keys = new HashMap<String, Set<String>>();
		
		Set<String> methods = makeCreator(GRAIN_MAX);
		keys.put(STAT_SERVICE_NAME, null);
		keys.put(STAT_MAX_MEMORY, methods);
		
		methods = makeCreator(GRAIN_AVG);
		keys.put(STAT_FREE_MEMORY, methods);
		keys.put(STAT_TOTAL_MEMORY, methods);
		keys.put(STAT_PROCESSORS, methods);
		keys.put(STAT_TIME_STAMP, makeCreator(GRAIN_AVG, GRAIN_MAX, GRAIN_MIN));
		return keys;
	}

	public Map<String, Object> getStatistics() {
		Map<String, Object> stats = createMap();
		Runtime runtime = Runtime.getRuntime();
		stats.put(STAT_MAX_MEMORY, runtime.maxMemory());
		stats.put(STAT_FREE_MEMORY, runtime.freeMemory());
		stats.put(STAT_TOTAL_MEMORY, runtime.totalMemory());
		stats.put(STAT_PROCESSORS, runtime.availableProcessors());
		stats.put(STAT_TIME_STAMP, System.currentTimeMillis());
		return stats;
	}
}
