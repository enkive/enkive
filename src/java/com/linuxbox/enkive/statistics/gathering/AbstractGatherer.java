/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INTERVAL;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_POINT;
import static com.linuxbox.enkive.statistics.VarsMaker.createMap;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;
import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;

public abstract class AbstractGatherer implements Gatherer {
	protected GathererAttributes attributes;
	protected StatsStorageService storageService;
	protected List<String> keys;
	private String gathererName;
	private String humanName;

	public AbstractGatherer(String gathererName, String humanName,
			List<String> keys) throws GathererException {
		this.gathererName = gathererName;
		this.humanName = humanName;
		setKeys(keys);
	}

	@Override
	public GathererAttributes getAttributes() {
		return attributes;
	}

	// BY DEFAULT ASSUME 15 MINUTE INTERVALS
	@Override
	public RawStats getStatistics() throws GathererException {
		int interval = 15;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		Date endDate = cal.getTime();
		cal.add(Calendar.MINUTE, -interval);
		Date startDate = cal.getTime();

		return getStatistics(startDate, endDate);
	}

	protected abstract Map<String, Object> getPointStatistics(
			Date startTimestamp, Date endTimestamp) throws GathererException;

	protected abstract Map<String, Object> getIntervalStatistics(
			Date startTimestamp, Date endTimestamp) throws GathererException;

	@Override
	public RawStats getStatistics(Date startTimestamp, Date endTimestamp)
			throws GathererException {
		if (startTimestamp == null || endTimestamp == null) {
			throw new GathererException(
					"A Date is null in getStatistics(Date, Date)");
		}

		Map<String, Object> intervalStats = getIntervalStatistics(
				startTimestamp, endTimestamp);
		Map<String, Object> pointStats = getPointStatistics(startTimestamp,
				endTimestamp);

		return new RawStats(intervalStats, pointStats, startTimestamp,
				endTimestamp);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RawStats getStatistics(List<String> intervalKeys,
			List<String> pointKeys) throws GathererException {
		if (intervalKeys == null && pointKeys == null) {
			throw new GathererException(
					"intervalKeys and pointKeys are both null in getStatistics(String[], String[])");
		}
		RawStats rawStats = getStatistics();
		Map<String, Object> data = rawStats.toMap();

		Map<String, Object> intervalData = null;
		Map<String, Object> intervalResult = null;

		if (data.containsKey(STAT_INTERVAL) && intervalKeys != null
				&& intervalKeys.size() != 0) {
			intervalData = (Map<String, Object>) data.get(STAT_INTERVAL);
			intervalResult = createMap();
			for (String statName : intervalKeys) {
				if (intervalData.containsKey(statName)) {
					intervalResult.put(statName, intervalData.get(statName));
				}
			}
		}

		Map<String, Object> pointData = null;
		Map<String, Object> pointResult = null;

		if (data.containsKey(STAT_POINT) && pointKeys != null
				&& pointKeys.size() != 0) {
			pointData = (Map<String, Object>) data.get(STAT_POINT);
			pointResult = createMap();
			for (String statName : pointKeys) {
				if (pointData.containsKey(statName)) {
					pointResult.put(statName, pointData.get(statName));
				}
			}
		}

		Date start = rawStats.getStartDate();
		Date end = rawStats.getEndDate();
		RawStats result = new RawStats(intervalResult, pointResult, start, end);

		return result;
	}

	/**
	 * builds the list of keyConsolidationHandlers that allow the attributes
	 * class to define how data is stored by this gatherer (and how it should be
	 * accessed for consolidation)
	 * 
	 * @param keyList
	 *            - a list of dot-notation formatted strings:
	 *            "coll.date:max,min,avg" the key's levels are specified by
	 *            periods and the key is separated from the methods by a colon.
	 *            An asterisk may be used as an 'any' to go down a level in a
	 *            map, such as: "*.date:max,min,avg"
	 * @return returns the instantiated consolidation list
	 * @throws GathererException
	 */
	protected List<ConsolidationKeyHandler> keyBuilder(List<String> keyList)
			throws GathererException {
		if (keyList == null) {
			throw new GathererException("keys were not set for " + gathererName);
		}

		List<ConsolidationKeyHandler> keys = new LinkedList<ConsolidationKeyHandler>();
		if (keyList != null) {
			for (String key : keyList) {
				keys.add(new ConsolidationKeyHandler(key));
			}
		}
		return keys;
	}

	@Override
	public void setStorageService(StatsStorageService storageService) {
		this.storageService = storageService;
	}

	@Override
	public void storeStats() throws GathererException {
		RawStats stats = getStatistics();
		storeStats(stats);
	}

	@Override
	public void storeStats(RawStats stats) throws GathererException {
		if (stats != null) {
			try {
				storageService.storeStatistics(attributes.getName(), stats);
			} catch (StatsStorageException e) {
				throw new GathererException(e);
			}
		}
	}

	public void setKeys(List<String> keys) throws GathererException {
		this.keys = keys;
		attributes = new GathererAttributes(gathererName, humanName,
				keyBuilder(keys));
	}
}
