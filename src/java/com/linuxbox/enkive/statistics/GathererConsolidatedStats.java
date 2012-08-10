//TODO
package com.linuxbox.enkive.statistics;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linuxbox.enkive.statistics.gathering.GathererAttributes;

public class GathererConsolidatedStats {
    abstract class ConsolidatedStats {
    	protected String name; //key name
        protected Map<String, Double> summaries; // e.g., "min", "max", ec.
		
		public abstract boolean isSingleStats();
    }
    
    class SingleConsolidatedStats extends ConsolidatedStats{
    	public SingleConsolidatedStats(String name, Map<String, Double> summaries){
    		this.name = name;
    		this.summaries = summaries;
    	}

		@Override
		public boolean isSingleStats() {
			return true;
		}
    }
    
    class MultiConsolidatedStats extends ConsolidatedStats{
    	protected Map<String, ConsolidatedStats> next = new HashMap<String, ConsolidatedStats>();
    	
    	@SuppressWarnings("unchecked")
		public MultiConsolidatedStats(List<String> path, Map<String, Object> dbMap){
    		String name = path.get(0);
			if(path.size() != 1){
				path.remove(path.size()-1);
				next.put(name, new MultiConsolidatedStats(path, (Map<String, Object>)dbMap.get(name)));
			} else {
				next.put(name, new SingleConsolidatedStats(name, (Map<String, Double>)dbMap.get(name)));
			}
    	}
    	
		@Override
		public boolean isSingleStats() {
			return false;
		}
    }
    
    String gatherer;
    Date startTime; // or should it be Calendar if we have time zone info?
    Date endTime;
    GathererAttributes attributes;
    Map<String, MultiConsolidatedStats> stats;//string is key of name in consolidatedStats
    
    public GathererConsolidatedStats(String gatherer, Date startTime, Date endTime, GathererAttributes attributes, Map<String, Object> dbMap){
    	this.gatherer = gatherer;
    	this.startTime = startTime;
    	this.endTime = endTime;
    	this.attributes = attributes;
    	setStats(dbMap);
    }
    
    private void setStats(Map<String, Object> dbMap){
    }
}