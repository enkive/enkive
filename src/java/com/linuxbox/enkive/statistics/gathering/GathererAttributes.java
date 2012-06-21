package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public class GathererAttributes {
	protected String serviceName;
	protected String schedule;
	protected Map<String, Set<String>> keys;
	protected long timeStamp;
	public GathererAttributes(String serviceName, String schedule, Map<String, Set<String>> keys){
		this.serviceName = serviceName;
		this.schedule = schedule;
		this.keys = keys;
	}
	
	public String getName(){
		return serviceName;
	}
	
	public void setTimeStamp(long timeStamp){
		this.timeStamp = timeStamp;
	}
	
	public long getTime(){
		return timeStamp;
	}
	
	public String getSchedule(){
		return schedule;
	}
	
	public Map<String, Set<String>> getKeys(){
		return keys;
	}
	
	public Map<String, Set<String>> getKeys(String keyName){
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		result.put(keyName, result.get(keyName));
		return result;
	}
}
