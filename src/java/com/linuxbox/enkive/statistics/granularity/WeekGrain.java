package com.linuxbox.enkive.statistics.granularity;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_DAY;

import java.util.Calendar;

import com.linuxbox.enkive.statistics.services.StatsClient;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.*;
//TODO IMPLEMENT
public class WeekGrain extends AbstractGrain{

	public WeekGrain(StatsClient client){
		this.client = client;
		start();
	}
	
	public void setFilterString(){
		filterString = GRAIN_DAY;
		grainType= GRAIN_WEEK;
	}
	
	public void setDates(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.DATE, Calendar.SUNDAY);
		endDate = cal.getTime();
		cal.add(Calendar.DATE, -7);
		startDate = cal.getTime();
	}
}
