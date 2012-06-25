package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.*;
import java.util.List;


import com.linuxbox.enkive.statistics.KeyDef;
public class GathererAttributes {
	protected String serviceName;
	protected String schedule;
	protected List<KeyDef> keys;
	
	public GathererAttributes(String serviceName, String schedule, List<KeyDef> keys){
		this.serviceName = serviceName;
		this.schedule = schedule;
		this.keys = keys;
		
		KeyDef name = new KeyDef(STAT_SERVICE_NAME + ":");
		KeyDef time = new KeyDef(STAT_TIME_STAMP + ":" + GRAIN_AVG + "," + GRAIN_MAX + "," + GRAIN_MIN);
		
		keys.add(name);
		keys.add(time);
	}
	
	public String getName(){
		return serviceName;
	}
	
	public String getSchedule(){
		return schedule;
	}
	
	public List<KeyDef> getKeys(){
		return keys;
	}
}
