package com.linuxbox.enkive.statistics;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TS_POINT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_POINT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INTERVAL;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RawStats {
	Map<String, Object> intervalStats;
	Date startDate = null;
	Date endDate   = null;
	
	Map<String, Object> pointStats;
	Date pointDate = null;
	
	public RawStats(Map<String, Object> iStats, Map<String, Object> pStats, Date startDate, Date endDate){
		setStartDate(startDate);
		setEndDate(endDate);
		setPointDate(new Date());
		setIntervalMap(iStats);
		setPointMap(pStats);
	}
	
	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setStartDate(Date timestamp) {
		this.startDate = timestamp;
	}

	public void setEndDate(Date timestamp) {
		this.endDate = timestamp;
	}
	
	public Date getPointDate() {
		return pointDate;
	}

	protected void setPointDate(Date timestamp) {
		this.pointDate = timestamp;
	}
    
	public Map<String, Object> toMap() {
		Map<String, Object> dateMap = new HashMap<String, Object>();
		dateMap.put(GRAIN_MIN, getStartDate());
		dateMap.put(STAT_TS_POINT, getPointDate());
		dateMap.put(GRAIN_MAX, getEndDate());
		
		Map<String, Object> statsMap = new HashMap<String, Object>();
		statsMap.put(STAT_TIMESTAMP, dateMap);
		if(intervalStats != null){
			statsMap.put(STAT_INTERVAL, intervalStats);
		}
		if(pointStats != null){
			statsMap.put(STAT_POINT, pointStats);
		}
		return statsMap;
	}
    
    public void setIntervalMap(Map<String, Object> stats){
    	this.intervalStats = stats;
    }
    
    public void setPointMap(Map<String, Object> stats){
    	this.pointStats = stats;
    }
}