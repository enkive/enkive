package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_PROCESSORS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MEMORY;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.linuxbox.enkive.statistics.KeyDef;

public class StatsRuntimeGatherer extends AbstractGatherer {

	public StatsRuntimeGatherer(String serviceName, String schedule) {
		super(serviceName, schedule);
	}

	@Override
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

	@Override
	protected List<KeyDef> keyBuilder() {
		List<KeyDef> keys = new LinkedList<KeyDef>();
		keys.add(new KeyDef(STAT_FREE_MEMORY + ":" + GRAIN_AVG + ","
				+ GRAIN_MAX + "," + GRAIN_MIN));
		keys.add(new KeyDef(STAT_MAX_MEMORY + ":" + GRAIN_AVG + "," + GRAIN_MAX
				+ "," + GRAIN_MIN));
		keys.add(new KeyDef(STAT_TOTAL_MEMORY + ":" + GRAIN_AVG + ","
				+ GRAIN_MAX + "," + GRAIN_MIN));
		keys.add(new KeyDef(STAT_PROCESSORS + ":" + GRAIN_AVG + "," + GRAIN_MAX
				+ "," + GRAIN_MIN));
		return keys;
	}
}
