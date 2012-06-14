package com.linuxbox.enkive.statistics.gathering;

import java.util.Map;
import static com.linuxbox.enkive.statistics.StatsConstants.*;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.*;
public class GathererAttributes {
	protected String serviceName;
	protected String schedule;
	protected Map<String, String> keys;
	
	public GathererAttributes(String serviceName, String schedule, Map<String, String> keys){
		this.serviceName = serviceName;
		this.schedule = schedule;
		this.keys = keys;
		keys.put(STAT_SERVICE_NAME, null);
		keys.put(STAT_TIME_STAMP, GRAIN_AVG);
	}
	
	public String getName(){
		return serviceName;
	}
	
	public String getSchedule(){
		return schedule;
	}
	
	public Map<String, String> getKeys(){
		return keys;
	}
}
