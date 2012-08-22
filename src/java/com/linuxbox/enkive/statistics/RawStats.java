package com.linuxbox.enkive.statistics;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_IS_POINT;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class RawStats {
	Map<String, Object> stats;
	int isPoint;
	public abstract Date getStartDate();
    public abstract Date getEndDate();
    protected abstract void setStartDate(Date timestamp);
    protected abstract void setEndDate(Date timestamp);
    
	public Map<String, Object> toMap() {
		Map<String, Object> dateMap = new HashMap<String, Object>();
		dateMap.put(GRAIN_MIN, getStartDate());
		dateMap.put(GRAIN_MAX, getEndDate());
		
		Map<String, Object> statsMap = getStatsMap();
		statsMap.put(STAT_TIMESTAMP, dateMap);
		statsMap.put(STAT_IS_POINT, isPoint);
		return statsMap;
	}
	
    public Map<String, Object> getStatsMap(){
    	return stats;
    }
    
    public void setStatsMap(Map<String, Object> stats){
    	this.stats = stats;
    }
}