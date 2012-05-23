package com.linuxbox.enkive.statistics.services;

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

import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoDBGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsGathererService extends AbstractService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");
	Map<String, AbstractGatherer> statsGatherers = null;

	public StatsGathererService(Map<String, AbstractGatherer> statsGatherers) {
		this.statsGatherers = statsGatherers;
	}

	public StatsGathererService(String serviceName, AbstractGatherer service) {
		statsGatherers = new HashMap<String, AbstractGatherer>();
		statsGatherers.put(serviceName, service);
	}

	// all stats
	public void addGatherer(String name, AbstractGatherer gatherer) {
		statsGatherers.put(name, gatherer);
	}

	public Set<Map<String, Object>> gatherStats() {
		return gatherStats(null);
	}

	// designated services & keys
	public Set<Map<String, Object>> gatherStats(Map<String, String[]> map) {
		if (map == null) {
			map = new HashMap<String, String[]>();
			for (String str : statsGatherers.keySet()) {
				map.put(str, null);
			}
		}

		Set<Map<String, Object>> statsSet = createSet();
		for (String name : map.keySet()) {
			long attributeTime = statsGatherers.get(name).attributes
					.getNextRunTime();
			long currTime = System.currentTimeMillis();
			// if not time skip that one
			if (attributeTime > currTime) {
				continue;
			}
			Map<String, Object> temp = createMap();
			if (map.get(name) != null)
				temp.put(name,
						statsGatherers.get(name).getStatistics(map.get(name)));
			else
				temp.put(name, statsGatherers.get(name).getStatistics());
			statsSet.add(temp);
		}
		return statsSet;
	}

	public static void main(String args[]) throws UnknownHostException,
			MongoException {
		StatsRuntimeGatherer runProps = new StatsRuntimeGatherer();
		StatsMongoAttachmentsGatherer attachProps = new StatsMongoAttachmentsGatherer(
				new Mongo(), "enkive", "fs");
		StatsMongoDBGatherer dbProps = new StatsMongoDBGatherer(new Mongo(),
				"enkive");
		StatsMongoCollectionGatherer collProps = new StatsMongoCollectionGatherer(
				new Mongo(), "enkive");

		Map<String, AbstractGatherer> gatherers = new HashMap<String, AbstractGatherer>();
		gatherers.put("runProps", runProps);
		gatherers.put("attachProps", attachProps);
		gatherers.put("dbProps", dbProps);
		gatherers.put("collProps", collProps);

		String[] keys = { STAT_TYPE, STAT_NAME, STAT_DATA_SIZE,
				STAT_AVG_ATTACH, STAT_FREE_MEMORY };

		Map<String, String[]> serviceKeys = new HashMap<String, String[]>();
		serviceKeys.put("runProps", keys);
		serviceKeys.put("attachProps", keys);
		serviceKeys.put("dbProps", keys);
		serviceKeys.put("collProps", keys);

		StatsGathererService service = new StatsGathererService(gatherers);
		System.out.println(service.gatherStats(null));
	}
}
