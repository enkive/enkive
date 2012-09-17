package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_PROCESSORS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MEMORY;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.linuxbox.enkive.statistics.VarsMaker.createMap;
import com.linuxbox.enkive.statistics.RawStats;

public class StatsRuntimeGatherer extends AbstractGatherer {	
	public StatsRuntimeGatherer(String serviceName, String humanName, List<String> keys) throws GathererException {
		super(serviceName, humanName, keys);		
	}

	@Override
	public RawStats getStatistics() {
		Map<String, Object> intervalStats = createMap();
		Runtime runtime = Runtime.getRuntime();
		intervalStats.put(STAT_MAX_MEMORY, runtime.maxMemory());
		intervalStats.put(STAT_FREE_MEMORY, runtime.freeMemory());
		intervalStats.put(STAT_TOTAL_MEMORY, runtime.totalMemory());
		intervalStats.put(STAT_PROCESSORS, runtime.availableProcessors());
		
		RawStats result = new RawStats(null, intervalStats, new Date(), new Date());
		return result;
	}
}
