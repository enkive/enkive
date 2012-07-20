package com.linuxbox.enkive.statistics;

import java.util.Map;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

public class StatsFilter {
	public String gathererName;
	public Map<String, Object> keys = null;
	
	public StatsFilter(String gathererName, Map<String, Object> keys){
		this.gathererName = gathererName;
		this.keys = keys;
		
		if(keys != null){
			keys.put(STAT_GATHERER_NAME, 1);
			keys.put(STAT_TIME_STAMP, 1);
		}
	}
}
