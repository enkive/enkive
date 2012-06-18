package com.linuxbox.enkive.statistics.gathering;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.linuxbox.enkive.statistics.StatsConstants.*;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.*;
public class GathererAttributes {
	protected String serviceName;
	protected String schedule;
	protected Map<String, Set<String>> keys;
	
	public GathererAttributes(String serviceName, String schedule, Map<String, Set<String>> keys){
		this.serviceName = serviceName;
		this.schedule = schedule;
		this.keys = keys;
		keys.put(STAT_SERVICE_NAME, null);
		
		Set<String> timeProperties = new HashSet<String>();
		timeProperties.add(GRAIN_AVG);
		timeProperties.add(GRAIN_MAX);
		timeProperties.add(GRAIN_MIN);
		
		keys.put(STAT_TIME_STAMP, timeProperties);
	}
	
	public Set<String> setCreator(String ... methodTypes){
		Set<String> result = new HashSet<String>();
		for(String methodName: methodTypes){
			result.add(methodName);
		}
		return result;
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
