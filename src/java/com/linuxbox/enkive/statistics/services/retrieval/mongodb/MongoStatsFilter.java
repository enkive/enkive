package com.linuxbox.enkive.statistics.services.retrieval.mongodb;

import java.util.Map;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;

//TODO Refactor this doesn't neet the gatherername so long as tracked by intex
public abstract class MongoStatsFilter {
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
	
	public abstract Map<String, Object> getFilter();
}
