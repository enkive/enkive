package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public class GathererAttributes {
	protected String serviceName;
	protected String schedule;
	protected Map<String, Set<String>> keys;
	
	public GathererAttributes(String serviceName, String schedule, Map<String, Set<String>> keys){
		this.serviceName = serviceName;
		this.schedule = schedule;
		this.keys = keys;
		
		Set<String> timeProperties = new HashSet<String>();
		timeProperties.add(GRAIN_AVG);
		timeProperties.add(GRAIN_MAX);
		timeProperties.add(GRAIN_MIN);
		
		keys.put(STAT_TIME_STAMP, timeProperties);
	}
	
	public String getName(){
		return serviceName;
	}
	
	public String getSchedule(){
		return schedule;
	}
	
	public Map<String, Set<String>> getKeys(){
		return keys;
	}
}
