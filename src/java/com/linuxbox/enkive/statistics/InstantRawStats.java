package com.linuxbox.enkive.statistics;

import java.util.Date;
import java.util.Map;

public class InstantRawStats extends RawStats{
	Date startDate = null;
	
	public InstantRawStats(Map<String, Object> stats, Date startDate){
		setStartDate(startDate);
		setEndDate(startDate);
		setStatsMap(stats);
	}
	
	@Override
	public Date getStartDate() {
		return startDate;
	}

	@Override
	public Date getEndDate() {
		return startDate;
	}

	@Override
	protected void setStartDate(Date timestamp) {
		this.startDate = timestamp;
	}

	@Override
	protected void setEndDate(Date timestamp) {
		setStartDate(timestamp);
	}
}
