package com.linuxbox.enkive.statistics.services.retrieval.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;

import java.util.Map;

import com.linuxbox.enkive.statistics.services.retrieval.StatsFilter;

public class MongoStatsFilter extends StatsFilter {

	public MongoStatsFilter(String gathererName, Map<String, Object> keys) {
		this.gathererName = gathererName;
		this.keys = keys;

		if (keys != null) {
			keys.put(STAT_GATHERER_NAME, 1);
			keys.put(STAT_TIMESTAMP, 1);
		}
	}

	@Override
	public Map<String, Object> getFilter() {
		return keys;
	}
}
