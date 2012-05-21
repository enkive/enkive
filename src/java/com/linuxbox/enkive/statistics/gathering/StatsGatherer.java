package com.linuxbox.enkive.statistics.gathering;

import java.util.Map;

public interface StatsGatherer {

	public Map<String, Object> getStatistics() throws StatsGatherException;

	public Map<String, Object> getStatistics(String[] keys)
			throws StatsGatherException;

}
