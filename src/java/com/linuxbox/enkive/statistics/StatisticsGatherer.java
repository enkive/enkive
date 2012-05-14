package com.linuxbox.enkive.statistics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

public class StatisticsGatherer {

	Map<String, StatisticsService> statisticsServices;

	public StatisticsGatherer() {
		statisticsServices = new HashMap<String, StatisticsService>();
	}

	public StatisticsGatherer(
			HashMap<String, StatisticsService> statisticsServices) {
		this.statisticsServices = statisticsServices;
	}

	public void registerStatisticsService(String serviceName,
			StatisticsService service) {
		statisticsServices.put(serviceName, service);
	}

	public JSONObject getStatisticsJSON() throws JSONException {
		JSONObject results = new JSONObject();
		Iterator<Entry<String, StatisticsService>> iterator = statisticsServices
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, StatisticsService> pair = (Map.Entry<String, StatisticsService>) iterator
					.next();
			results.append(pair.getKey(), pair.getValue().getStatisticsJSON());
		}
		return results;
	}

}
