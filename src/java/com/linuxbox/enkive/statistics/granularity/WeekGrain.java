package com.linuxbox.enkive.statistics.granularity;

import java.util.Calendar;

import com.linuxbox.enkive.statistics.services.StatsClient;
public class WeekGrain extends EmbeddedGrain{

	public WeekGrain(StatsClient client){
		super(client);
	}
	
	public void setDates(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR, 0);
		while(cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
			cal.add(Calendar.DATE, -1);
		}
		endDate = cal.getTime();
		cal.add(Calendar.DATE, -7);
		startDate = cal.getTime();
	}
}
