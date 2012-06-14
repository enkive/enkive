package com.linuxbox.enkive.statistics.granularity;

import java.util.Calendar;

import com.linuxbox.enkive.statistics.services.StatsClient;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.*;

public class MonthGrain extends AbstractGrain {
	
	public MonthGrain(StatsClient client){
		this.client = client;
		start();
	}
	
	public void setFilterString(){
		filterString = GRAIN_DAY;
		grainType = GRAIN_MONTH;	
	}
	
	public void setDates(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.DATE, 1);
		endDate = cal.getTime();
		cal.add(Calendar.MONTH, -1);
		startDate = cal.getTime();
	}
}
