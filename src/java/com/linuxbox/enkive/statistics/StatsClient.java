package com.linuxbox.enkive.statistics;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.linuxbox.enkive.statistics.gathering.StatsGatherService;
import com.linuxbox.enkive.statistics.retrieval.StatsRetrievalException;
import com.linuxbox.enkive.statistics.retrieval.StatsRetrievalService;
import com.linuxbox.enkive.statistics.storage.StatsStorageException;
import com.linuxbox.enkive.statistics.storage.StatsStorageService;

public class StatsClient {
	protected StatsGatherService gatherService;
	protected StatsStorageService storageService;
	protected StatsRetrievalService retrievalService;

	public StatsClient(StatsGatherService gatherService,
			StatsStorageService storageService,
			StatsRetrievalService retrievalService) {
		this.gatherService = gatherService;
		this.storageService = storageService;
		this.retrievalService = retrievalService;
	}

	public Set<Map<String, Object>> retrieveData(Map<String, String[]> statNames,
			Date startingTimestamp, Date endingTimestamp) throws StatsRetrievalException {
		return retrievalService.queryStatistics(statNames, startingTimestamp, endingTimestamp);
	}

	public void storeData(Set<Map<String, Object>> set) {
		try {
			storageService.storeStatistics(set);
		} catch (StatsStorageException e) {
			//TODO: handle this
			System.exit(0);
		}
	}

	public Set<Map<String, Object>> gatherData(Map<String, String[]> map) {
		return gatherService.gatherStats(map);
	}
}
