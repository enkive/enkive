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
package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INDEX_SIZES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_LAST_EXTENT_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_INDEX;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_OBJS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_SIZE;
import static com.linuxbox.enkive.statistics.VarsMaker.createMap;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_COUNT;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_INDEX_SIZES;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_LAST_EXTENT_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_NS;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_NUM_INDEX;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_STORAGE_SIZE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_TOTAL_INDEX_SIZE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.mongodb.DB;
import com.mongodb.MongoClient;

public class MongoStatsCollectionGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering");

	protected DB db;

	protected MongoClient m;

	public MongoStatsCollectionGatherer(MongoClient m, String dbName,
			String gathererName, String humanName, List<String> keys)
			throws GathererException {
		super(gathererName, humanName, keys);
		this.m = m;
		db = m.getDB(dbName);
	}

	@Override
	public RawStats getStatistics() {
		Map<String, Object> pointStats = new HashMap<String, Object>();
		for (String collName : db.getCollectionNames()) {
			String key = collName;
			if (collName.startsWith("$")) {
				collName = collName.replaceFirst("$", "-");
			}
			collName = collName.replace('.', '-');
			pointStats.put(collName, getPointStats(key));
		}

		RawStats result = new RawStats(null, pointStats, new Date(), new Date());
		return result;
	}

	@Override
	public RawStats getStatistics(List<String> intervalKeys,
			List<String> pointKeys) {
		Map<String, Object> pointResult = null;
		Map<String, Object> intervalResult = null;

		for (String collName : db.getCollectionNames()) {
			Map<String, Object> pointData = getPointStats(collName);
			Map<String, Object> intervalData = getIntervalStats(collName);

			if (intervalData != null && intervalKeys != null
					&& intervalKeys.size() != 0) {
				Map<String, Object> filteredIntervalData = new HashMap<String, Object>();
				for (String statName : intervalKeys) {
					if (intervalData.containsKey(statName)) {
						filteredIntervalData.put(statName,
								intervalData.get(statName));
					}
				}
				if (!filteredIntervalData.isEmpty()) {
					if (intervalResult == null) {
						intervalResult = new HashMap<String, Object>();
					}
					intervalResult.put(collName, filteredIntervalData);
				}
			}

			if (pointData != null && pointKeys != null && pointKeys.size() != 0) {
				Map<String, Object> filteredPointData = new HashMap<String, Object>();
				for (String statName : pointKeys) {
					if (pointData.containsKey(statName)) {
						filteredPointData
								.put(statName, pointData.get(statName));
					}
				}
				if (!filteredPointData.isEmpty()) {
					if (pointResult == null) {
						pointResult = new HashMap<String, Object>();
					}
					pointResult.put(collName, filteredPointData);
				}
			}
		}

		RawStats result = new RawStats(intervalResult, pointResult, new Date(),
				new Date());
		return result;
	}

	/**
	 * gets the point statistics cooresponding to a given collection
	 * 
	 * @param collectionName
	 *            - the name of the collection on which to gather stats
	 * @return the stats collected
	 */
	private Map<String, Object> getPointStats(String collectionName) {
		if (db.collectionExists(collectionName)) {
			Map<String, Object> stats = createMap();
			Map<String, Object> temp = db.getCollection(collectionName)
					.getStats();
			stats.put(STAT_NS, temp.get(MONGO_NS));
			stats.put(STAT_NUM_OBJS, temp.get(MONGO_COUNT));
			stats.put(STAT_AVG_OBJ_SIZE, temp.get(MONGO_AVG_OBJ_SIZE));
			stats.put(STAT_DATA_SIZE, temp.get(MONGO_SIZE));
			stats.put(STAT_TOTAL_SIZE, temp.get(MONGO_STORAGE_SIZE));
			stats.put(STAT_NUM_EXTENT, temp.get(MONGO_NUM_EXTENT));
			stats.put(STAT_LAST_EXTENT_SIZE, temp.get(MONGO_LAST_EXTENT_SIZE));
			stats.put(STAT_NUM_INDEX, temp.get(MONGO_NUM_INDEX));
			stats.put(STAT_TOTAL_INDEX_SIZE, temp.get(MONGO_TOTAL_INDEX_SIZE));
			stats.put(STAT_INDEX_SIZES, temp.get(MONGO_INDEX_SIZES));
			return stats;
		} else {
			LOGGER.warn("Collection " + collectionName + " could not be found");
			return null;
		}
	}

	/**
	 * gets the interval statistics cooresponding to a given collection
	 * 
	 * @param collectionName
	 *            - the name of the collection on which to gather stats
	 * @return the stats collected
	 */
	private Map<String, Object> getIntervalStats(String collectionName) {
		return null;
	}

	@Override
	protected Map<String, Object> getPointStatistics(Date startTimestamp,
			Date endTimestamp) throws GathererException {
		Map<String, Object> pointStats = createMap();
		for (String collName : db.getCollectionNames()) {
			String key = collName;
			if (collName.startsWith("$")) {
				collName = collName.replaceFirst("$", "-");
			}
			collName = collName.replace('.', '-');
			pointStats.put(collName, getPointStats(key));
		}
		return pointStats;
	}

	@Override
	protected Map<String, Object> getIntervalStatistics(Date startTimestamp,
			Date endTimestamp) throws GathererException {
		return null;
	}

}
