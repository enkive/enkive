package com.linuxbox.enkive.statistics;

import java.util.Date;
import java.util.Map;

public class RawStats {
    Date timestamp; // or should it be Calendar? do we have time zone info?
    Map<String, Object> stats;
    
    public Date getTimestamp(){
    	return timestamp;
    }
    
    public void setTimestamp(Date timestamp){
    	this.timestamp = timestamp;
    }
    
    public Map<String, Object> getStatsMap(){
    	return stats;
    }
    
    public void setStatsMap(Map<String, Object> stats){
    	this.stats = stats;
    }    
}