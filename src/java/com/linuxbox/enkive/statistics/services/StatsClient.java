package com.linuxbox.enkive.statistics.services;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.services.retrieval.StatsRetrievalException;
import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;

public class StatsClient {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.services.StatsClient");
	protected StatsGathererService gathererService;
	protected StatsRetrievalService retrievalService;
	protected StatsStorageService storageService;

	public StatsClient(StatsGathererService gatherer,
			StatsStorageService storer, StatsRetrievalService retriever) {
		gathererService = gatherer;
		storageService = storer;
		retrievalService = retriever;
		LOGGER.info("Client successfully created");
	}

	public Set<Map<String, Object>> directQuery(Map<String, Object> stats) {
		return retrievalService.directQuery(stats);
	}

	public Set<Map<String, Object>> gatherData() {
		try {
			return gathererService.gatherStats();
		} catch (ParseException e) {
			LOGGER.warn("Client.gatherData() ParseException", e);
		} catch (GathererException e) {
			LOGGER.warn("Client.gatherData() GathererException", e);
		}

		return null;
	}

	public Set<Map<String, Object>> gatherData(Map<String, String[]> map) {
		try {
			return gathererService.gatherStats(map);
		} catch (ParseException e) {
			LOGGER.error("Client.gatherData(Map) ParseException", e);
		} catch (GathererException e) {
			LOGGER.error("Client.gatherData(Map) GathererException", e);
		}
		return null;
	}

	public Set<String> gathererNames() {
		return gathererService.getStatsGatherers().keySet();
	}

	public Set<GathererAttributes> getAttributes() {
		Set<GathererAttributes> attributeSet = new HashSet<GathererAttributes>();
		for (String name : gathererNames()) {
			attributeSet.add(getAttributes(name));
		}
		return attributeSet;
	}

	public GathererAttributes getAttributes(String serviceName) {
		return gathererService.getStatsGatherers(serviceName).get(serviceName)
				.getAttributes();
	}

	public Set<Map<String, Object>> queryStatistics(
			Map<String, Map<String, Object>> stats, Date startingTimestamp,
			Date endingTimestamp) {
		try {
			return retrievalService.queryStatistics(stats, startingTimestamp,
					endingTimestamp);
		} catch (StatsRetrievalException e) {
			LOGGER.error(
					"Client.queryStatistics(Map, Date, Date) StatsRetrievalException",
					e);
		}
		return null;
	}
	
	public Set<Map<String, Object>> queryStatistics(Map<String, Map<String, Object>> queryMap, Map<String, Map<String, Object>> filterMap){
		try {
			return retrievalService.queryStatistics(queryMap, filterMap);
		} catch (StatsRetrievalException e) {
			LOGGER.error(
					"Client.queryStatistics(Map, Date, Date) StatsRetrievalException",
					e);
		}
		return null;
	}

	public void remove(Set<Object> deletionSet) {
		try {
			retrievalService.remove(deletionSet);
		} catch (StatsRetrievalException e) {
			LOGGER.error("Client.remove(Set) StatsRetrievalException", e);
		}
	}

	public void storeData(Set<Map<String, Object>> set) {
		try {
			storageService.storeStatistics(set);
		} catch (StatsStorageException e) {
			LOGGER.error("Client.storeData StatsStorageException", e);
		}
	}
}
