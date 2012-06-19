package com.linuxbox.enkive.statistics.granularity;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_HOUR;

import java.util.Calendar;

import com.linuxbox.enkive.statistics.services.StatsClient;

public class HourGrain extends AbstractGrain {
	
	public HourGrain(StatsClient client){
		super(client);
	}
	public void setFilterString(){
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
