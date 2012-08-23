package com.linuxbox.enkive.statistics;

import java.util.Date;
import java.util.Map;

public class PointRawStats extends RawStats{
	Date startDate = null;
	Date pointDate = null;
	
	public PointRawStats(Map<String, Object> stats, Date startDate){
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
	public void setStartDate(Date timestamp) {
		this.startDate = timestamp;
	}

	@Override
	public void setEndDate(Date timestamp) {
		setStartDate(timestamp);
	}

	@Override
	public Date getPointDate() {
		return pointDate;
	}

	@Override
	protected void setPointDate(Date timestamp) {
		this.pointDate = timestamp;
	}
}
