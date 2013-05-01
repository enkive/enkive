/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.TestingConstants.MONGODB_TEST_DATABASE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FILE_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INTERVAL;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_COLLECTIONS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_INDEX;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_OBJS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_POINT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_SIZE;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;
import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.gathering.mongodb.MongoStatsDatabaseGatherer;
import com.mongodb.Mongo;

public class StatsMongoDbTest {
	private static MongoStatsDatabaseGatherer dbStats;
	private static Map<String, Object> stats;
	private static Map<String, Object> intervalStats;
	private static Map<String, Object> pointStats;
	private static Mongo m;
	private static String name = "DBGatherer";

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		m = new Mongo();
		List<String> keys = new LinkedList<String>();
		keys.add("db::Database Name::");
		keys.add("numObj:avg,max,min:Number of Objects::point");
		keys.add("nColls:avg,max,min:Number of Collections::point");
		keys.add("avgOSz:avg,max,min:Average Object Size:bytes:point");
		keys.add("dataSz:avg,max,min:Data Size:bytes:point");
		keys.add("totSz:avg,max,min:Total Size:bytes:point");
		keys.add("numInd:avg,max,min:Number of Indexes::point");
		keys.add("indSz:avg,max,min:Index Size:objects:point");
		keys.add("numExt:avg,max,min:Number of Extents::point");
		keys.add("fileSz:avg,max,min:File Size:bytes:point");
		dbStats = new MongoStatsDatabaseGatherer(m, MONGODB_TEST_DATABASE, name,
				"Database Statistics", keys);
		RawStats rawStats = dbStats.getStatistics();
		stats = rawStats.toMap();
		if (stats.containsKey(STAT_INTERVAL)) {
			intervalStats = (Map<String, Object>) stats.get(STAT_INTERVAL);
		}
		if (stats.containsKey(STAT_POINT)) {
			pointStats = (Map<String, Object>) stats.get(STAT_POINT);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unchecked")
	public boolean checkFormat(Map<String, Object> stats,
			LinkedList<String> path) {
		if (path.contains(STAT_GATHERER_NAME)) {
			return true;
		}

		if (path.isEmpty() || stats == null) {
			return false;
		}
		String key = path.getFirst();
		if (path.size() == 1) {
			if (key.equals("*"))
				return stats != null;
			else {
				return stats.get(key) != null;
			}
		}

		boolean result = false;
		if (key.equals("*")) {
			path.removeFirst();
			for (String statKey : stats.keySet()) {
				if (!(stats.get(statKey) instanceof Map)) {
					result = path.size() == 1;
				} else {
					result = checkFormat(
							(Map<String, Object>) stats.get(statKey), path);
				}
				if (result) {
					break;
				}
			}
			path.addFirst(key);
			return result;
		} else if (stats.containsKey(key)) {
			path.removeFirst();
			result = checkFormat((Map<String, Object>) stats.get(key), path);
			path.addFirst(key);
			return result;
		}
		return false;
	}

	@Test
	public void attributesNotNull() {
		assertTrue("dbStats.getAttributes returned null",
				dbStats.getAttributes() != null);
	}

	@Test
	public void testAttributes() {
		for (ConsolidationKeyHandler key : dbStats.getAttributes().getKeys()) {
			LinkedList<String> path = key.getKey();
			if (path.contains(STAT_GATHERER_NAME)
					|| path.contains(STAT_TIMESTAMP)) {
				continue;
			}
			if (key.isPoint() || path.contains(STAT_NAME)) {
				assertTrue("the format is incorrect for path: " + path,
						checkFormat(pointStats, path));
			} else {
				assertTrue("the format is incorrect for path: " + path,
						checkFormat(intervalStats, path));
			}
		}
	}

	@Test
	public void pointKeyCountMatches() {
		int numKeys = 0;
		int pointKeyCount = 0;
		for (ConsolidationKeyHandler key : dbStats.getAttributes().getKeys()) {
			if (key.isPoint()) {
				pointKeyCount++;
			}
		}
		numKeys += pointStats.keySet().size();// can't think of a good way to
												// determine the name so -1
		if (pointStats.containsKey(STAT_NAME)) {
			numKeys = numKeys - 1;
		}
		assertTrue("numKeys doesn't match: numKeys = " + numKeys,
				numKeys == pointKeyCount);
	}

	@Test
	public void intervalKeyCountMatches() {
		int numKeys = 0;
		int intervalKeyCount = 0;
		for (ConsolidationKeyHandler key : dbStats.getAttributes().getKeys()) {
			if (!key.isPoint() && key.getMethods() != null) {
				intervalKeyCount++;
			}
		}
		if (intervalStats != null) {
			numKeys += intervalStats.keySet().size();
		}
		assertTrue("numKeys doesn't match: numKeys = " + numKeys,
				numKeys == intervalKeyCount);
	}

	@Test
	public void hasServiceName() {
		String sn = (String) dbStats.getAttributes().getName();
		assertNotNull("no service name found in hasServiceName()", sn);
		assertTrue(sn.equals(name));
	}

	@Test
	public void hasTimeStamp() {
		@SuppressWarnings("unchecked")
		Map<String, Object> time = (Map<String, Object>) stats
				.get(STAT_TIMESTAMP);
		assertTrue("runtime test exception in hasTimeStamp(): time = " + time,
				time != null);
	}

	@Test
	public void upperTimeGTZero() {
		@SuppressWarnings("unchecked")
		Map<String, Object> time = (Map<String, Object>) stats
				.get(STAT_TIMESTAMP);
		Date date = ((Date) time.get(CONSOLIDATION_MAX));
		assertTrue("runtime test exception in timeGTZero(): date = " + date,
				date.getTime() > 0);
	}

	@Test
	public void lowerTimeGTZero() {
		@SuppressWarnings("unchecked")
		Map<String, Object> time = (Map<String, Object>) stats
				.get(STAT_TIMESTAMP);
		Date date = ((Date) time.get(CONSOLIDATION_MIN));
		assertTrue("runtime test exception in timeGTZero(): date = " + date,
				date.getTime() > 0);
	}

	@Test
	public void nameTest() {
		String name = (String) pointStats.get(STAT_NAME);
		System.out.println(stats);
		assertNotNull("in " + MONGODB_TEST_DATABASE + " (type = null)", name);
		assertTrue("in " + MONGODB_TEST_DATABASE + " (type = " + name + ")",
				name.compareTo(MONGODB_TEST_DATABASE) == 0);
	}

	// GT means 'greater than'
	@Test
	public void numCollsGTZero() {
		assertNotNull("in " + MONGODB_TEST_DATABASE + " (numColls = null)",
				pointStats.get(STAT_NUM_COLLECTIONS));
		int numColls = ((Integer) pointStats.get(STAT_NUM_COLLECTIONS))
				.intValue();
		assertTrue("in " + MONGODB_TEST_DATABASE + " (numColls = " + numColls
				+ ") ", numColls > 0);
	}

	@Test
	public void numObjsGTZero() {
		assertNotNull("in " + MONGODB_TEST_DATABASE + " (numObjs = null)",
				pointStats.get(STAT_NUM_OBJS));
		int numObjs = ((Integer) pointStats.get(STAT_NUM_OBJS)).intValue();
		assertTrue("in " + MONGODB_TEST_DATABASE + " (numObjs = " + numObjs
				+ ") ", numObjs > 0);
	}

	@Test
	public void numAvgObjSizeGTZero() {
		assertNotNull("in " + MONGODB_TEST_DATABASE + " (avgObjSize = null)",
				pointStats.get(STAT_AVG_OBJ_SIZE));
		double avgObjSize = (Double) ((Double) pointStats
				.get(STAT_AVG_OBJ_SIZE)).doubleValue();
		assertTrue("in " + MONGODB_TEST_DATABASE + " (avgObjSize = "
				+ avgObjSize + ") ", avgObjSize > 0);
	}

	@Test
	public void dataGTZero() {
		assertNotNull("in " + MONGODB_TEST_DATABASE + " (data = null)",
				pointStats.get(STAT_DATA_SIZE));
		int data = ((Integer) pointStats.get(STAT_DATA_SIZE)).intValue();
		assertTrue("in " + MONGODB_TEST_DATABASE + " (data = " + data + ") ",
				data > 0);
	}

	@Test
	public void storageGTZero() {
		assertNotNull("in " + MONGODB_TEST_DATABASE + " (storage = null)",
				pointStats.get(STAT_TOTAL_SIZE));
		int storage = ((Integer) pointStats.get(STAT_TOTAL_SIZE)).intValue();
		assertTrue("in " + MONGODB_TEST_DATABASE + " (storage = " + storage
				+ ") ", storage > 0);
	}

	@Test
	public void numIndexGTZero() {
		assertNotNull("in " + MONGODB_TEST_DATABASE + " (numIndex = null)",
				pointStats.get(STAT_NUM_INDEX));
		int numIndex = ((Integer) pointStats.get(STAT_NUM_INDEX)).intValue();
		assertTrue("in " + MONGODB_TEST_DATABASE + " (numIndex = " + numIndex
				+ ") ", numIndex > 0);
	}

	@Test
	public void totalIndexSizeGTZero() {
		assertNotNull("in " + MONGODB_TEST_DATABASE
				+ " (totalIndexSize = null)",
				pointStats.get(STAT_TOTAL_INDEX_SIZE));
		int totalIndexSize = ((Integer) pointStats.get(STAT_TOTAL_INDEX_SIZE))
				.intValue();
		assertTrue("in " + MONGODB_TEST_DATABASE + " (totalIndexSize = "
				+ totalIndexSize + ") ", totalIndexSize > 0);
	}

	@Test
	public void numExtentGTZero() {
		assertNotNull("in " + MONGODB_TEST_DATABASE + " (numExtents = null)",
				pointStats.get(STAT_NUM_EXTENT));
		int numExtents = ((Integer) pointStats.get(STAT_NUM_EXTENT)).intValue();
		assertTrue("in " + MONGODB_TEST_DATABASE + " (numExtents = "
				+ numExtents + ") ", numExtents > 0);
	}

	@Test
	public void fileSizeGTZero() {
		assertNotNull("in " + MONGODB_TEST_DATABASE + " (fileSize = null)",
				pointStats.get(STAT_FILE_SIZE));
		int fileSize = ((Integer) pointStats.get(STAT_FILE_SIZE)).intValue();
		assertTrue("in " + MONGODB_TEST_DATABASE + " (fileSize = " + fileSize
				+ ") ", fileSize > 0);
	}
}
