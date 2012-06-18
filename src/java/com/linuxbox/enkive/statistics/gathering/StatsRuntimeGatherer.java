package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_PROCESSORS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.*;
public class StatsRuntimeGatherer extends AbstractGatherer {
	
	public StatsRuntimeGatherer(String serviceName, String schedule){
		super(serviceName, schedule);
	}
	
	protected Map<String, Set<String>> keyBuilder(){
		Map<String, Set<String>> keys = new HashMap<String, Set<String>>();
		
		Set<String> methods = setCreator(GRAIN_MAX);
		keys.put(STAT_MAX_MEMORY, methods);
		
		methods = setCreator(GRAIN_AVG);
		keys.put(STAT_FREE_MEMORY, methods);
		keys.put(STAT_TOTAL_MEMORY, methods);
		keys.put(STAT_PROCESSORS, methods);
		return keys;
	}
	
	public Map<String, Object> getStats() {
		Map<String, Object> stats = createMap();
		Runtime runtime = Runtime.getRuntime();
		stats.put(STAT_MAX_MEMORY, runtime.maxMemory());
		stats.put(STAT_FREE_MEMORY, runtime.freeMemory());
		stats.put(STAT_TOTAL_MEMORY, runtime.totalMemory());
		stats.put(STAT_PROCESSORS, runtime.availableProcessors());
		stats.put(STAT_TIME_STAMP, System.currentTimeMillis());
		return stats;
	}

	public Map<String, Object> getStatistics() {
		return getStats();
	}

	public static void main(String args[]) {
		StatsRuntimeGatherer runProps = new StatsRuntimeGatherer("SERVICENAME", "CRONEXPRESSION");
		System.out.println(runProps.getStatistics());
		String[] keys = { STAT_TYPE, STAT_NAME, STAT_DATA_SIZE,
				STAT_TOTAL_MEMORY, STAT_FREE_MEMORY };
		System.out.println(runProps.getStatistics(keys));
	}
}
