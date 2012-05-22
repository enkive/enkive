package com.linuxbox.enkive.statistics.services;

import java.util.Map;
import java.util.Set;

import com.linuxbox.enkive.statistics.storage.StatsStorageException;

public class StatsClient {
	protected StatsGathererService gathererService;
	protected StatsStorageService storageService;
	
	public StatsClient(StatsGathererService gatherer, StatsStorageService storer){
		gathererService = gatherer;
		storageService  = storer;
	}
	
	//TODO: add attributes checking
	public Set<Map<String, Object>> gatherData(){
		return gathererService.gatherStats(); 
	}
	
	public void storeData(Set<Map<String, Object>> set) throws StatsStorageException{
		storageService.storeStatistics(set);
	}
	
	public void gatherAndStoreData() throws StatsStorageException{
		storeData(gatherData());
	}
}
