package com.linuxbox.enkive.statistics.services;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_ATTACH;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.GathererInterface;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoDBGatherer;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsGathererService extends AbstractService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.services");

	public static void main(String args[]) throws UnknownHostException,
			MongoException {
		StatsRuntimeGatherer runProps = new StatsRuntimeGatherer("SERVICENAME",
				"CRONEXPRESSION");
		StatsMongoAttachmentsGatherer attachProps = new StatsMongoAttachmentsGatherer(
				new Mongo(), "enkive", "fs", "AttachName", "cronExpress");
		StatsMongoDBGatherer dbProps = new StatsMongoDBGatherer(new Mongo(),
				"enkive", "attachName", "cronexpress");
		StatsMongoCollectionGatherer collProps = new StatsMongoCollectionGatherer(
				new Mongo(), "enkive", "collName", "cronExpression");

		Map<String, GathererInterface> gatherers = new HashMap<String, GathererInterface>();
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

		try {
			new StatsGathererService(gatherers);
		} catch (Exception e) {
			System.exit(0);
		}
	}

	protected Map<String, GathererInterface> statsGatherers = null;

	public StatsGathererService(Map<String, GathererInterface> statsGatherers)
			throws ParseException {
		this.statsGatherers = statsGatherers;
	}

	public StatsGathererService(String serviceName, GathererInterface gatherer)
			throws ParseException {
		statsGatherers = new HashMap<String, GathererInterface>();
		statsGatherers.put(serviceName, gatherer);
	}

	public void addGatherer(String name, GathererInterface gatherer) {
		if (statsGatherers != null) {
			statsGatherers.put(name, gatherer);
		} else {
			statsGatherers = new HashMap<String, GathererInterface>();
			statsGatherers.put(name, gatherer);
		}
	}

	public Set<Map<String, Object>> gatherStats() throws ParseException,
			GathererException {
		return gatherStats(null);
	}

	public Set<Map<String, Object>> gatherStats(
			Map<String, String[]> gathererKeys) throws ParseException,
			GathererException {
		if (statsGatherers == null) {
			return null;
		}

		if (statsGatherers.isEmpty()) {
			return null;
		}

		if (gathererKeys == null) {
			gathererKeys = new HashMap<String, String[]>();
			for (String gathererName : statsGatherers.keySet()) {
				gathererKeys.put(gathererName, null);
			}
		}

		Set<Map<String, Object>> statsSet = createSet();
		for (String name : gathererKeys.keySet()) {
			statsSet.add(statsGatherers.get(name).getStatistics(
					gathererKeys.get(name)));
		}

		return statsSet;
	}

	public Map<String, GathererInterface> getStatsGatherers() {
		return statsGatherers;
	}

	public Map<String, GathererInterface> getStatsGatherers(String name) {
		Map<String, GathererInterface> gathererMap = new HashMap<String, GathererInterface>();
		gathererMap.put(name, statsGatherers.get(name));
		return gathererMap;
	}

	@PostConstruct
	public void init() {
		String info = "GathererService created with gatherers:";
		if (getStatsGatherers() != null) {
			for (String name : getStatsGatherers().keySet()) {
				info = info + " " + name;
			}
		}
		LOGGER.info(info);
	}

	public void removeGatherer(String name) {
		if (statsGatherers.containsKey(name)) {
			statsGatherers.remove(name);
		}
	}
}
