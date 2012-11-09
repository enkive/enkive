/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
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
 ******************************************************************************/
package com.linuxbox.enkive.statistics.consolidation;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INTERVAL;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_POINT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.VarsMaker.createLinkedListOfStrs;
import static com.linuxbox.enkive.statistics.VarsMaker.createListOfMaps;
import static com.linuxbox.enkive.statistics.VarsMaker.createMap;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_HOUR;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_RAW;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_SUM;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_TYPE;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.retrieval.StatsFilter;
import com.linuxbox.enkive.statistics.services.retrieval.StatsQuery;
import com.linuxbox.enkive.statistics.services.retrieval.StatsTypeFilter;
import com.linuxbox.enkive.statistics.services.retrieval.mongodb.MongoStatsQuery;

public class HourConsolidator extends AbstractConsolidator {
	public HourConsolidator(StatsClient client) {
		super(client);
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getStatTypeData(String gathererName,
			String type) {
		StatsQuery query = new MongoStatsQuery(gathererName, filterType, type,
				startDate, endDate);
		StatsFilter filter = new StatsTypeFilter(type);
		List<Map<String, Object>> result = createListOfMaps();
		Set<Map<String, Object>> queryData = client.queryStatistics(query,filter);
		for (Map<String, Object> statsMap : queryData) {
			statsMap.remove("_id");// WARNING mongo specific pollution

			if (statsMap != null && !statsMap.isEmpty()) {
				result.add(new HashMap<String, Object>(
						(Map<String, Object>) statsMap.get(type)));
			}
		}
		return result;
	}

	public List<List<Map<String, Object>>> gathererFilter(String gathererName) {
		List<List<Map<String, Object>>> result = new LinkedList<List<Map<String, Object>>>();

		// interval stats
		List<Map<String, Object>> intervalData = getStatTypeData(gathererName,
				STAT_INTERVAL);
		if (!intervalData.isEmpty()) {
			result.add(intervalData);
		} else {
			result.add(null);
		}

		// point stats
		List<Map<String, Object>> pointData = getStatTypeData(gathererName,
				STAT_POINT);
		if (!pointData.isEmpty()) {
			result.add(pointData);
		} else {
			result.add(null);
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> consolidateData() {
		if (endDate == null || startDate == null) {
			LOGGER.error("Cannot run consolidateData until dates are set");
			throw new NullPointerException();
		}
		List<Map<String, Object>> storageData = createListOfMaps();
		for (GathererAttributes attribute : client.getAttributes()) {
			String name = attribute.getName();
			List<List<Map<String, Object>>> serviceData = gathererFilter(name);
			if (!serviceData.isEmpty()) {
				Map<String, Object> mapToStore = createMap();

				// Interval
				List<Map<String, Object>> intervalData = serviceData.get(0);
				if (intervalData != null) {
					Map<String, Object> intervalTemplate = createMap((Map<String, Object>) intervalData
							.iterator().next());
					Map<String, Object> intervalMapToStore = createMap(intervalTemplate);

					List<ConsolidationKeyHandler> intervalKeys = new LinkedList<ConsolidationKeyHandler>();
					for (ConsolidationKeyHandler keyDef : attribute.getKeys()) {
						if (!keyDef.isPoint()) {
							intervalKeys.add(keyDef);
						}
					}

					generateConsolidatedMap(intervalTemplate,
							intervalMapToStore, createLinkedListOfStrs(),
							intervalKeys, intervalData);
					mapToStore.putAll(intervalMapToStore);
				}
				// Point
				List<Map<String, Object>> pointData = serviceData.get(1);
				if (pointData != null) {
					Map<String, Object> pointTemplate = createMap((Map<String, Object>) pointData
							.iterator().next());
					Map<String, Object> pointMapToStore = createMap(pointTemplate);

					List<ConsolidationKeyHandler> pointKeys = new LinkedList<ConsolidationKeyHandler>();
					for (ConsolidationKeyHandler keyDef : attribute.getKeys()) {
						if (keyDef.isPoint()) {
							pointKeys.add(keyDef);
						}
					}

					generateConsolidatedMap(pointTemplate, pointMapToStore,
							new LinkedList<String>(), pointKeys, pointData);
					mapToStore.putAll(pointMapToStore);
				}

				if (!mapToStore.isEmpty()) {
					Map<String, Object> dateMap = createMap();
					dateMap.put(CONSOLIDATION_MIN, startDate);
					dateMap.put(CONSOLIDATION_MAX, endDate);

					mapToStore.put(STAT_TIMESTAMP, dateMap);
					mapToStore.put(CONSOLIDATION_TYPE, consolidationType);
					mapToStore.put(STAT_GATHERER_NAME, attribute.getName());
					storageData.add(mapToStore);
				}
			}
		}
		return storageData;
	}

	@Override
	protected void consolidateMaps(Map<String, Object> consolidatedData,
			List<Map<String, Object>> serviceData,
			ConsolidationKeyHandler keyDef, LinkedList<String> dataPath) {

		if (keyDef.getMethods() != null) {
			boolean isPoint = keyDef.isPoint();
			DescriptiveStatistics statsMaker = new DescriptiveStatistics();
			Object dataVal = null;

			// get data from maps and add to statMakers
			for (Map<String, Object> dataMap : serviceData) {
				dataVal = getDataVal(dataMap, dataPath);
				double input = -1;

				if (dataVal != null) {
					input = statToDouble(dataVal);
					if (input > -1) {
						statsMaker.addValue(input);
					}
				}
			}

			// loop over methods to populate map with max, min, etc.
			Map<String, Object> methodData = createMap();
			for (String method : keyDef.getMethods()) {
				if (!method.equals(CONSOLIDATION_SUM)) {// may not be user
														// defined
					methodMapBuilder(method, statsMaker, methodData);
				}
			}
			if (!isPoint) {// create sum
				methodMapBuilder(CONSOLIDATION_SUM, statsMaker, methodData);
			}
			// store in new map on path
			putOnPath(dataPath, consolidatedData, methodData);
		}
	}

	@Override
	public void setDates() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		Date upperDate = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, -1);
		Date lowerDate = cal.getTime();
		setDates(upperDate, lowerDate);
	}

	@Override
	public void setTypes() {
		setTypes(CONSOLIDATION_HOUR, CONSOLIDATION_RAW);
	}
	
	
}
