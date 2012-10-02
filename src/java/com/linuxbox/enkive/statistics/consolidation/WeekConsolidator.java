package com.linuxbox.enkive.statistics.consolidation;

import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_DAY;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_WEEK;

import java.util.Calendar;
import java.util.Date;

import com.linuxbox.enkive.statistics.services.StatsClient;

public class WeekConsolidator extends EmbeddedConsolidator {

	public WeekConsolidator(StatsClient client) {
		super(client);
	}

	@Override
	public void setDates() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			cal.add(Calendar.DATE, -1);
		}
		Date upperDate = cal.getTime();
		cal.add(Calendar.DATE, -7);
		Date lowerDate = cal.getTime();
		setDates(upperDate, lowerDate);
	}
	
	@Override
	public void setTypes() {
		setTypes(CONSOLIDATION_WEEK, CONSOLIDATION_DAY);
	}
}
