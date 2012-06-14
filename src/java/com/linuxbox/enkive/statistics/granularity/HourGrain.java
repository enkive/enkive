package com.linuxbox.enkive.statistics.granularity;

import java.util.Calendar;

import com.linuxbox.enkive.statistics.services.StatsClient;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.*;

public class HourGrain extends AbstractGrain {
	
	public HourGrain(StatsClient client){
		this.client = client;
		start();
	}
	
	public void setFilterString(){
		filterString = null;
		grainType = GRAIN_HOUR;
	}
	
	public void setDates(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		endDate = cal.getTime();	
		cal.add(Calendar.HOUR_OF_DAY, -1);
		startDate = cal.getTime();
	}
}
