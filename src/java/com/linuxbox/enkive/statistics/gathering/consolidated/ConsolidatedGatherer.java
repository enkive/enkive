package com.linuxbox.enkive.statistics.gathering.consolidated;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_DAY;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_HOUR;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MONTH;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_WEEK;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.retrieval.StatsQuery;

public abstract class ConsolidatedGatherer {
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
	public ConsolidatedGatherer(String name, StatsClient client, int hrKeepTime, int dayKeepTime, int weekKeepTime, int monthKeepTime) {
		this.client = client;
		this.gathererName = name;
		this.hrKeepTime = hrKeepTime;
		this.dayKeepTime = dayKeepTime;
		this.weekKeepTime = weekKeepTime;
		this.monthKeepTime = monthKeepTime;
		statDate = getEarliestStatisticDate();
	}

	protected abstract void init();
	
	protected void setEndDate(int grain){
		Calendar end = Calendar.getInstance();
		end.set(Calendar.MILLISECOND, 0);
		end.set(Calendar.SECOND, 0);
		end.set(Calendar.MINUTE, 0);
		end.add(Calendar.HOUR_OF_DAY, -hrKeepTime);
		
		if(grain == GRAIN_HOUR){
			end.add(Calendar.HOUR_OF_DAY, -hrKeepTime);
		} else if(grain == GRAIN_DAY){
			end.set(Calendar.HOUR_OF_DAY, 0);
			end.add(Calendar.DATE,-dayKeepTime); 
		} else if(grain == GRAIN_WEEK){		
			end.set(Calendar.HOUR_OF_DAY, 0);
			while(end.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
				end.add(Calendar.DATE, -1);
			}
			end.add(Calendar.WEEK_OF_YEAR, -weekKeepTime);
		} else if(grain == GRAIN_MONTH){
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
		setEndDate(GRAIN_HOUR);
		if(c.getTimeInMillis() > endDate.getTime()){
			client.storeData(consolidatePast(GRAIN_HOUR, c));
		}
	}
	
	public void consolidatePastDays(){
		Calendar c = Calendar.getInstance();
		c.setTime(statDate);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		setEndDate(GRAIN_DAY);
		if(c.getTimeInMillis() > endDate.getTime()){
			client.storeData(consolidatePast(GRAIN_DAY, c));
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
		setEndDate(GRAIN_WEEK);
		if(c.getTimeInMillis() > endDate.getTime()){
			client.storeData(consolidatePast(GRAIN_WEEK, c));
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
		setEndDate(GRAIN_MONTH);
		if(c.getTimeInMillis() > endDate.getTime()){
			client.storeData(consolidatePast(GRAIN_MONTH, c));
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Date getEarliestStatisticDate(){
		StatsQuery query = new StatsQuery(gathererName, null);
		Calendar earliestDate = Calendar.getInstance();
		for(Map<String, Object> statMap: client.queryStatistics(query)){
			if(statMap.get(GRAIN_TYPE) != null){
				Map<String, Object> tsMap = (Map<String, Object>)statMap.get(STAT_TIMESTAMP);
				Date tempDate = (Date)tsMap.get(GRAIN_MIN);
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
	
	public Set<Map<String,Object>> consolidatePast(int grain, Calendar c){
		Set<Map<String, Object>> result = new HashSet<Map<String, Object>>();
//TODO
		System.out.print("grain: " + grain);
		while(c.getTimeInMillis() > endDate.getTime()){
			Date end = c.getTime();
			if(grain == GRAIN_HOUR){
				c.add(Calendar.HOUR_OF_DAY, -1);
			} else if(grain == GRAIN_DAY){
				c.add(Calendar.DATE, -1);
			} else if(grain == GRAIN_WEEK){
				c.add(Calendar.DATE, -7);
			} else if(grain == GRAIN_MONTH){
				c.add(Calendar.MONTH, -1);
			}
			Date start = c.getTime();
			try {
				Map<String, Object> consolidated = getConsolidatedData(start, end, grain);
				if(consolidated != null){
					result.add(consolidated);
				} else {
					System.out.println("null: start:" + start + "end: " + end);
					break;
				}
			} catch (GathererException e) {
				LOGGER.error("Consolidated gatherer error on range " + start + " to " + end, e);
			}
		}
//TODO
		System.out.println(" : " + result.size());
		return result;
	}
	
	protected abstract Map<String, Object> getConsolidatedData(Date start, Date end, int grain) throws GathererException;
}
