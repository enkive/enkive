package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import java.util.Map;


import com.linuxbox.enkive.statistics.AbstractStatsService;

public abstract class StatsAbstractGatherer extends AbstractStatsService implements StatsGatherer {
	
	public abstract Map<String, Object> getStatistics();
	
	public Map<String, Object> getStatistics(String[] keys) {
		if (keys == null)
			return getStatistics();
		Map<String, Object> stats = getStatistics();
		Map<String, Object> selectedStats = createMap();
		for (String key : keys) {
			if (stats.get(key) != null)
				selectedStats.put(key, stats.get(key));
		}
		if(selectedStats.get(STAT_TIME_STAMP) != null)
			selectedStats.put(STAT_TIME_STAMP,  selectedStats.get(STAT_TIME_STAMP));
		else
			selectedStats.put(STAT_TIME_STAMP, System.currentTimeMillis());

		return selectedStats;
	}
}
