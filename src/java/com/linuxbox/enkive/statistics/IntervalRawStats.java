package com.linuxbox.enkive.statistics;

import java.util.Date;
import java.util.Map;

public class IntervalRawStats extends RawStats{
	Date startDate = null;
	Date endDate = null;
	
	public IntervalRawStats(Map<String, Object> stats, Date startDate, Date endDate){
		setStartDate(startDate);
		setEndDate(endDate);
		setStatsMap(stats);
		isPoint = 0;
	}
	
	@Override
	public Date getStartDate() {
		return startDate;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	@Override
	public void setStartDate(Date timestamp) {
		this.startDate = timestamp;
	}

	@Override
	public void setEndDate(Date timestamp) {
		this.endDate = timestamp;
	}
}
