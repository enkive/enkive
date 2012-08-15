package com.linuxbox.enkive.statistics;

import java.util.Date;
import java.util.Map;

public class RangedRawStats extends RawStats{
	Date startDate = null;
	Date endDate = null;
	
	public RangedRawStats(Map<String, Object> stats, Date startDate, Date endDate){
		setStartDate(startDate);
		setEndDate(endDate);
		setStatsMap(stats);
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
	protected void setStartDate(Date timestamp) {
		this.startDate = timestamp;
	}

	@Override
	protected void setEndDate(Date timestamp) {
		this.endDate = timestamp;
	}
}
