package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_PROCESSORS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MEMORY;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.linuxbox.enkive.statistics.InstantRawStats;
import com.linuxbox.enkive.statistics.VarsMaker;
import com.linuxbox.enkive.statistics.RawStats;

public class StatsRuntimeGatherer extends AbstractGatherer {

	public StatsRuntimeGatherer(String serviceName, String humanName, String schedule) {
		super(serviceName, humanName, schedule);
	}
	
	public StatsRuntimeGatherer(String serviceName, String humanName, String schedule, List<String> keys) throws GathererException {
		super(serviceName, humanName, schedule, keys);		
	}

	@Override
	public RawStats getStatistics() {
		Map<String, Object> stats = VarsMaker.createMap();
		Runtime runtime = Runtime.getRuntime();
		stats.put(STAT_MAX_MEMORY, runtime.maxMemory());
		stats.put(STAT_FREE_MEMORY, runtime.freeMemory());
		stats.put(STAT_TOTAL_MEMORY, runtime.totalMemory());
		stats.put(STAT_PROCESSORS, runtime.availableProcessors());
		
		RawStats result = new InstantRawStats(stats, new Date());
		return result;
	}
}
