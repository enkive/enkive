package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_ATTACH;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.*;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.AbstractStatsService;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsGatherService extends AbstractStatsService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");
	Map<String, StatsGatherer> statsGatherers = null;

	public StatsGatherService(Map<String, StatsGatherer> statsGatherers) {
		this.statsGatherers = statsGatherers;
	}

	public StatsGatherService(String serviceName, StatsGatherer service) {
		statsGatherers = new HashMap<String, StatsGatherer>();
		statsGatherers.put(serviceName, service);
	}

	// all stats
	public void addGatherer(String name, StatsGatherer gatherer){
		statsGatherers.put(name, gatherer);
	}
	
	public Set<Map<String, Object>> gatherStats() {
		Set<Map<String, Object>> statsSet = createSet();

		for (String name : statsGatherers.keySet()) {
			Map<String, Object> temp = createMap();
			try {
				temp.put(name, statsGatherers.get(name).getStatistics());
			} catch (StatsGatherException e) {
				LOGGER.warn("could not gather stats for " + name, e);
			}
			statsSet.add(temp);
		}
		return statsSet;
	}

	// designated services & keys
	public Set<Map<String, Object>> gatherStats(Map<String, String[]> map) {
		if (map == null) {
			return gatherStats();
		}

		Set<Map<String, Object>> statsSet = createSet();
		for (String name : map.keySet()) {
			Map<String, Object> temp = createMap();;
			try {
				if (map.get(name) != null)
					temp.put(
							name,
							statsGatherers.get(name).getStatistics(
									map.get(name)));
				else
					temp.put(name, statsGatherers.get(name).getStatistics());
			} catch (StatsGatherException e) {
				LOGGER.warn("could not gather stats for " + name, e);
				temp.put("ERROR", null);
			}
			statsSet.add(temp);
		}
		return statsSet;
	}

	public static void main(String args[]) throws UnknownHostException, MongoException{
		StatsRuntimeProperties runProps   = new StatsRuntimeProperties();
		StatsMongoAttachments attachProps = new StatsMongoAttachments(new Mongo(), "enkive", "fs");
		StatsMongoDBProperties dbProps    = new StatsMongoDBProperties(new Mongo(), "enkive");
		StatsMongoCollectionProperties collProps = new StatsMongoCollectionProperties(new Mongo(), "enkive");
		
		Map<String, StatsGatherer> gatherers = new HashMap<String, StatsGatherer>();
		gatherers.put("runProps", runProps);
		gatherers.put("attachProps", attachProps);
		gatherers.put("dbProps", dbProps);
		gatherers.put("collProps", collProps);
		
		String[] keys = {STAT_TYPE, STAT_NAME, STAT_DATA_SIZE, STAT_AVG_ATTACH, STAT_FREE_MEMORY };
		
		Map<String, String[]> serviceKeys = new HashMap<String, String[]>();
		serviceKeys.put("runProps", keys);
		serviceKeys.put("attachProps", keys);
		serviceKeys.put("dbProps", keys);
		serviceKeys.put("collProps", keys);
		
		StatsGatherService service = new StatsGatherService(gatherers);
		System.out.println(service.gatherStats(serviceKeys));
	}

}
