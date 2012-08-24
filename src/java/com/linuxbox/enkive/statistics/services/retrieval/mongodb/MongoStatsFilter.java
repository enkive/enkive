package com.linuxbox.enkive.statistics.services.retrieval.mongodb;

import java.util.Map;

import com.linuxbox.enkive.statistics.services.retrieval.StatsFilter;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;

public class MongoStatsFilter extends StatsFilter {
	public String gathererName;
	public Map<String, Object> keys = null;
	
	public MongoStatsFilter(String gathererName, Map<String, Object> keys){
		this.gathererName = gathererName;
		this.keys = keys;
		
		if(keys != null){
			keys.put(STAT_GATHERER_NAME, 1);
			keys.put(STAT_TIMESTAMP, 1);
		}
	}

	@Override
	public Map<String, Object> getFilter() {
		return keys;
	}
}
