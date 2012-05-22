package com.linuxbox.enkive.statistics.gathering;

import java.util.Map;

public class GathererAttributes {
	private long timeInterval;
	private long nextRunTime;
	private Map<String, Object> defaultMap;
	
	public GathererAttributes(long interval, long start, Map<String, Object> map){
		timeInterval = interval;
		nextRunTime = start;
		defaultMap = map;
	}
	
	public long incrementTime(){
		nextRunTime += timeInterval;
		return nextRunTime;
	}

	public long getNextRunTime(){
		return nextRunTime;
	}
	
	public Map<String, Object> getDefaultMap(){
		return defaultMap;
	}
	
	
}
