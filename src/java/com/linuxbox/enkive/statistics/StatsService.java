package com.linuxbox.enkive.statistics;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.statistics.gathering.StatsGatherer;


public class StatsService {

	Map<String, StatsGatherer> gatherers;

	public StatsService() {
		gatherers = new HashMap<String, StatsGatherer>();
	}

	public StatsService(HashMap<String, StatsGatherer> statisticsServices) {
		this.gatherers = statisticsServices;
	}

	public void registerStatisticsService(String serviceName,
			StatsGatherer service) {
		gatherers.put(serviceName, service);
	}

	public JSONObject getStatisticsJSON() throws JSONException {
		JSONObject results = new JSONObject();

		/*
		 * NOAH: starting with Java 5, the following iterator + loop can be
		 * changed into the loop below. They're essentially equivalent except
		 * the 2nd one is shorter and easier to read.
		 */
		// Iterator<Entry<String, StatsService>> iterator = statisticsServices
		// .entrySet().iterator();
		// while (iterator.hasNext()) {
		// Map.Entry<String, StatsService> pair = (Map.Entry<String,
		// StatsService>) iterator
		// .next();
		// results.put(pair.getKey(), pair.getValue().getStatisticsJSON());
		// }

		for (Map.Entry<String, StatsGatherer> pair : gatherers
				.entrySet()) {
			results.put(pair.getKey(), pair.getValue().getStatisticsJSON());
		}

		return results;
	}
}
