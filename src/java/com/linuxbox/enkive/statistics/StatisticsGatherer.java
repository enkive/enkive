/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
