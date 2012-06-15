package com.linuxbox.enkive.statistics.services;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.quartz.SchedulerException;

import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.retrieval.StatsRetrievalException;
import com.linuxbox.enkive.statistics.storage.StatsStorageException;

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
		try {
			storageService.storeStatistics(set);
		} catch (StatsStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void gatherAndStoreData() {
		storeData(gatherData());
	}

	public Set<Map<String, Object>> queryStatistics(
			Map<String, Map<String, Object>> stats, Date startingTimestamp,
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
		Set<GathererAttributes> attributeSet = new HashSet<GathererAttributes>();
		for(String name: gathererNames()){
			attributeSet.add(getAttributes(name));
		}
		return attributeSet;
	}
	
	public GathererAttributes getAttributes(String serviceName){
		return gathererService.getStatsGatherers(serviceName).get(serviceName).getAttributes();
	}
	
	public void remove(Set<Object> deletionSet){
		try {
			retrievalService.remove(deletionSet);
		} catch (StatsRetrievalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
