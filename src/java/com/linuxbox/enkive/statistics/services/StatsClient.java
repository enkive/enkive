package com.linuxbox.enkive.statistics.services;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.quartz.SchedulerException;

import com.linuxbox.enkive.statistics.retrieval.StatsRetrievalException;
import com.linuxbox.enkive.statistics.storage.StatsStorageException;

public class StatsClient {
	protected StatsGathererService gathererService;
	protected StatsStorageService storageService;
	protected StatsRetrievalService retrievalService;
	
	public StatsClient(StatsGathererService gatherer, StatsStorageService storer, StatsRetrievalService retriever) {
		gathererService = gatherer;
		storageService = storer;
		retrievalService = retriever;
	}
	
	// TODO: add attributes checking
	public Set<Map<String, Object>> gatherData() throws ParseException, SchedulerException {
		return gathererService.gatherStats();
	}

	public void storeData(Set<Map<String, Object>> set)
			throws StatsStorageException {
		storageService.storeStatistics(set);
	}

	public void gatherAndStoreData() throws StatsStorageException, ParseException, SchedulerException {
		storeData(gatherData());
	}
	
	public Set<Map<String, Object>> queryStatistics(
			Map<String, String[]> stats, Date startingTimestamp,
			Date endingTimestamp) throws StatsRetrievalException {
		return retrievalService.queryStatistics(stats, startingTimestamp, endingTimestamp);
	}
}
