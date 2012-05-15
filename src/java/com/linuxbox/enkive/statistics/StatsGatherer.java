package com.linuxbox.enkive.statistics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

public class StatsGatherer {

	Map<String, StatsService> statisticsServices;

	public StatsGatherer() {
		statisticsServices = new HashMap<String, StatsService>();
	}

	public StatsGatherer(
			HashMap<String, StatsService> statisticsServices) {
		this.statisticsServices = statisticsServices;
	}

	public void registerStatisticsService(String serviceName,
			StatsService service) {
		statisticsServices.put(serviceName, service);
	}

	public JSONObject getStatisticsJSON() throws JSONException {
		JSONObject results = new JSONObject();
		Iterator<Entry<String, StatsService>> iterator = statisticsServices
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, StatsService> pair = (Map.Entry<String, StatsService>) iterator
					.next();
			results.put(pair.getKey(), pair.getValue().getStatisticsJSON());
		}
		return results;
	}
}
