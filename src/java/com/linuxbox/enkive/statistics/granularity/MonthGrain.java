package com.linuxbox.enkive.statistics.granularity;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_DAY;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MONTH;

import java.util.Calendar;
import java.util.Date;

import com.linuxbox.enkive.statistics.services.StatsClient;

public class MonthGrain extends EmbeddedGrain {

	public MonthGrain(StatsClient client) {
		super(client);
	}

	@Override
	public void setDates() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.DATE, 1);
		Date upperDate = cal.getTime();
		cal.add(Calendar.MONTH, -1);
		Date lowerDate = cal.getTime();
		setDates(upperDate, lowerDate);
	}
	
	@Override
	public void setTypes() {
		setTypes(GRAIN_MONTH, GRAIN_DAY);
	}
}
