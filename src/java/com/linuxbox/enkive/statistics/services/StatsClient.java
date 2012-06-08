package com.linuxbox.enkive.statistics.services;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.quartz.SchedulerException;

import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.retrieval.StatsRetrievalException;
//TODO: statsClient must handle all the exeptions
public class StatsClient {
	protected StatsGathererService gathererService;
	protected StatsStorageService storageService;
	protected StatsRetrievalService retrievalService;

	public StatsClient(StatsGathererService gatherer,
			StatsStorageService storer, StatsRetrievalService retriever) {
		gathererService = gatherer;
		storageService = storer;
		retrievalService = retriever;
	}

	public Set<Map<String, Object>> gatherData(){
		try {
			return gathererService.gatherStats();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public void storeData(Set<Map<String, Object>> set){
		System.out.println("Don't do that! (StatsClient-storeData())");
	}

	public void gatherAndStoreData() {
		storeData(gatherData());
	}

	public Set<Map<String, Object>> queryStatistics(
			Map<String, String[]> stats, Date startingTimestamp,
			Date endingTimestamp) {
		try {
			return retrievalService.queryStatistics(stats, startingTimestamp,
					endingTimestamp);
		} catch (StatsRetrievalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Set<String> gathererNames(){
		return gathererService.getStatsGatherers().keySet();
	}
	
	public Set<GathererAttributes> getAttributes(){
		Map<String, AbstractGatherer> gatherers = gathererService.getStatsGatherers();
		Set<GathererAttributes> attributeSet = new HashSet<GathererAttributes>();
		for(String name: gathererNames()){
			GathererAttributes attribute = gatherers.get(name).getAttributes();
			attributeSet.add(attribute);
		}
		return attributeSet;
	}
	
	public void remove(Set<Map<String, Object>> deletionSet){
		try {
			retrievalService.remove(deletionSet);
		} catch (StatsRetrievalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
