package com.linuxbox.enkive.statistics.consolidation;

import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_DAY;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_HOUR;

import java.util.Calendar;
import java.util.Date;

import com.linuxbox.enkive.statistics.services.StatsClient;

public class DayConsolidator extends EmbeddedConsolidator {

	public DayConsolidator(StatsClient client) {
		super(client);
	}

	@Override
	public void setDates() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		Date upperDate = cal.getTime();
		cal.add(Calendar.DATE, -1);
		Date lowerDate = cal.getTime();
		setDates(upperDate, lowerDate);
//TODO		
		setDates(new Date(), lowerDate);
	}

	@Override
	public void setTypes() {
		setTypes(CONSOLIDATION_DAY, CONSOLIDATION_HOUR);
	}
}
