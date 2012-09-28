package com.linuxbox.enkive.statistics.gathering.past;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INTERVAL;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_DAY;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_HOUR;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MONTH;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_TYPE;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_WEEK;

import java.util.Calendar;
import java.util.Date;
import java.util.HashList;
import java.util.Map;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.retrieval.StatsQuery;
import com.linuxbox.enkive.statistics.services.retrieval.mongodb.MongoStatsQuery;

public abstract class PastGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.messagesConsolidatedGatherer");
	protected Date endDate;
	protected Date statDate;
	protected StatsClient client;
	protected String gathererName;
	protected int hrKeepTime;
	protected int dayKeepTime;
	protected int weekKeepTime;
	protected int monthKeepTime;
//TODO use gatherer to gather statistics instead of redundant code
//TODO ^populate with spring^
//TODO
	public PastGatherer(String name, StatsClient client, int hrKeepTime, int dayKeepTime, int weekKeepTime, int monthKeepTime) {
		this.client = client;
		this.gathererName = name;
		this.hrKeepTime = hrKeepTime;
		this.dayKeepTime = dayKeepTime;
		this.weekKeepTime = weekKeepTime;
		this.monthKeepTime = monthKeepTime;
		statDate = getEarliestStatDate();
	}

	protected abstract void init();
	
	protected void setEndDate(int grain){
		Calendar end = Calendar.getInstance();
		end.set(Calendar.MILLISECOND, 0);
		end.set(Calendar.SECOND, 0);
		end.set(Calendar.MINUTE, 0);
		if(grain == CONSOLIDATION_HOUR){
			end.add(Calendar.HOUR_OF_DAY, -hrKeepTime);
		} else if(grain == CONSOLIDATION_DAY){
			end.set(Calendar.HOUR_OF_DAY, 0);
			end.add(Calendar.DATE,-dayKeepTime); 
		} else if(grain == CONSOLIDATION_WEEK){		
			end.set(Calendar.HOUR_OF_DAY, 0);
			while(end.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
				end.add(Calendar.DATE, -1);
			}
			end.add(Calendar.WEEK_OF_YEAR, -weekKeepTime);
		} else if(grain == CONSOLIDATION_MONTH){
			end.set(Calendar.HOUR_OF_DAY, 0);
			end.set(Calendar.DAY_OF_MONTH, 1);
			end.add(Calendar.MONTH, -monthKeepTime);
		}
		this.endDate = end.getTime();
	}
	
	public void consolidatePastHours(){
		Calendar c = Calendar.getInstance();
		c.setTime(statDate);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		setEndDate(CONSOLIDATION_HOUR);
		
		if(c.getTimeInMillis() > endDate.getTime()){
			client.storeData(consolidatePast(CONSOLIDATION_HOUR, c));
		}
	}
	
	public void consolidatePastDays(){
		Calendar c = Calendar.getInstance();
		c.setTime(statDate);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		setEndDate(CONSOLIDATION_DAY);
		if(c.getTimeInMillis() > endDate.getTime()){
			client.storeData(consolidatePast(CONSOLIDATION_DAY, c));
		}
	}
	
	public void consolidatePastWeeks(){
		Calendar c = Calendar.getInstance();
		c.setTime(statDate);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		while(c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
			c.add(Calendar.DATE, -1);
		}
		setEndDate(CONSOLIDATION_WEEK);
		if(c.getTimeInMillis() > endDate.getTime()){
			client.storeData(consolidatePast(CONSOLIDATION_WEEK, c));
		}
	}
	
	public void consolidatePastMonths(){
		Calendar c = Calendar.getInstance();
		c.setTime(statDate);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);
		setEndDate(CONSOLIDATION_MONTH);
		if(c.getTimeInMillis() > endDate.getTime()){
			client.storeData(consolidatePast(CONSOLIDATION_MONTH, c));
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Date getEarliestStatDate(){
		StatsQuery query = new MongoStatsQuery(gathererName, STAT_INTERVAL, new Date(0L), new Date());
		Calendar earliestDate = Calendar.getInstance();
		for(Map<String, Object> statMap: client.queryStatistics(query)){
			if(statMap.get(CONSOLIDATION_TYPE) != null){
				Map<String, Object> tsMap = (Map<String, Object>)statMap.get(STAT_TIMESTAMP);
				Date tempDate = (Date)tsMap.get(CONSOLIDATION_MIN);
				if(earliestDate.getTimeInMillis() > tempDate.getTime()){
					earliestDate.setTime(tempDate);
				}
			}
		}
		earliestDate.set(Calendar.MILLISECOND, 0);
		earliestDate.set(Calendar.SECOND, 0);
		earliestDate.set(Calendar.MINUTE, 0);
		return earliestDate.getTime();
	}
	
	public List<Map<String,Object>> consolidatePast(int grain, Calendar c){
		List<Map<String, Object>> result = new HashList<Map<String, Object>>();
		
		while(c.getTimeInMillis() > endDate.getTime()){
			Date end = c.getTime();
			if(grain == CONSOLIDATION_HOUR){
				c.add(Calendar.HOUR_OF_DAY, -1);
			} else if(grain == CONSOLIDATION_DAY){
				c.add(Calendar.DATE, -1);
			} else if(grain == CONSOLIDATION_WEEK){
				c.add(Calendar.DATE, -7);
			} else if(grain == CONSOLIDATION_MONTH){
				c.add(Calendar.MONTH, -1);
			}
			Date start = c.getTime();
			try {
				Map<String, Object> consolidated = getConsolidatedData(start, end, grain);
				if(consolidated != null){
					result.add(consolidated);
				} else {
					break;
				}
			} catch (GathererException e) {
				LOGGER.error("Consolidated gatherer error on range " + start + " to " + end, e);
			}
		}
		return result;
	}
	
	protected abstract Map<String, Object> getConsolidatedData(Date start, Date end, int grain) throws GathererException;
}
