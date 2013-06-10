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
import static com.linuxbox.enkive.TestingConstants.MONGODB_TEST_FSFILES_COLLECTION;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_NUM;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INTERVAL;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_POINT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
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
import com.linuxbox.enkive.statistics.gathering.mongodb.MongoStatsAttachmentsGatherer;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class StatsMongoAttachTest {
	protected static MongoStatsAttachmentsGatherer attach;
	private static Map<String, Object> stats;
	private static String name = "AttachmentGatherer";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		List<String> keys = new LinkedList<String>();
		keys.add("attNum:avg:Number of Attachments::interval");
		keys.add("attSz:avg:Attachment Size:bytes:interval");
		attach = new MongoStatsAttachmentsGatherer(new MongoClient(),
				MONGODB_TEST_DATABASE, MONGODB_TEST_FSFILES_COLLECTION, name,
				"Attachment Statistics", keys);
		RawStats rawData = attach.getStatistics();
		stats = rawData.toMap();
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

		if (path.isEmpty()) {
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

	@SuppressWarnings("unchecked")
	@Test
	public void testAttributes() {
		for (ConsolidationKeyHandler key : attach.getAttributes().getKeys()) {
			LinkedList<String> path = key.getKey();
			if (path.contains(STAT_GATHERER_NAME)
					|| path.contains(STAT_TIMESTAMP)) {
				continue;
			}
			if (key.isPoint()) {
				assertTrue(
						"the format is incorrect for path: " + path,
						checkFormat(
								(Map<String, Object>) stats.get(STAT_POINT),
								path));
			} else {
				assertTrue(
						"the format is incorrect for path: " + path,
						checkFormat(
								(Map<String, Object>) stats.get(STAT_INTERVAL),
								path));
			}
		}
	}

	@Test
	public void pointKeyCountMatches() {
		int numKeys = 0;
		int pointKeyCount = 0;
		for (ConsolidationKeyHandler key : attach.getAttributes().getKeys()) {
			if (key.isPoint() && key.getMethods() != null) {
				pointKeyCount++;
			}
		}

		if (stats.containsKey(STAT_POINT)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> pointStats = (Map<String, Object>) stats
					.get(STAT_POINT);
			numKeys += pointStats.keySet().size();
		}

		assertTrue("numKeys doesn't match: numKeys = " + numKeys,
				numKeys == pointKeyCount);
	}

	@Test
	public void intervalKeyCountMatches() {
		int numKeys = 0;
		int intervalKeyCount = 0;
		for (ConsolidationKeyHandler key : attach.getAttributes().getKeys()) {
			if (!key.isPoint() && key.getMethods() != null) {
				intervalKeyCount++;
			}
		}

		if (stats.containsKey(STAT_INTERVAL)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> intervalStats = (Map<String, Object>) stats
					.get(STAT_INTERVAL);
			numKeys += intervalStats.keySet().size();
		}
		System.out.println("intervalKeyCount: " + intervalKeyCount);
		System.out.println("numKeys: " + numKeys);
		assertTrue("numKeys doesn't match: numKeys = " + numKeys,
				numKeys == intervalKeyCount);
	}

	@Test
	public void hasServiceName() {
		String sn = (String) attach.getAttributes().getName();
		assertNotNull("no service name found in hasServiceName()", sn);
		assertTrue(sn.equals(name));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void hasTimeStamp() {
		Map<String, Object> time = (Map<String, Object>) stats
				.get(STAT_TIMESTAMP);
		assertTrue("runtime test exception in hasTimeStamp(): time = " + time,
				time != null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void upperTimeGTZero() {
		Map<String, Object> time = (Map<String, Object>) stats
				.get(STAT_TIMESTAMP);
		Date date = ((Date) time.get(CONSOLIDATION_MAX));
		assertTrue("runtime test exception in timeGTZero(): date = " + date,
				date.getTime() > 0);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void lowerTimeGTZero() {
		Map<String, Object> time = (Map<String, Object>) stats
				.get(STAT_TIMESTAMP);
		Date date = ((Date) time.get(CONSOLIDATION_MIN));
		assertTrue("runtime test exception in timeGTZero(): date = " + date,
				date.getTime() > 0);
	}

	@Test
	public void testAttachSize() throws UnknownHostException, MongoException {
		@SuppressWarnings("unchecked")
		Map<String, Object> intervalStats = (Map<String, Object>) stats
				.get(STAT_INTERVAL);
		Object attachSz = intervalStats.get(STAT_ATTACH_SIZE);
		assertTrue("(max = " + attachSz + ")",
				((Long) attachSz).longValue() >= 0.0);
	}

	@Test
	public void testAttNum() throws UnknownHostException, MongoException {
		@SuppressWarnings("unchecked")
		Map<String, Object> intervalStats = (Map<String, Object>) stats
				.get(STAT_INTERVAL);
		Object attachNum = intervalStats.get(STAT_ATTACH_NUM);
		assertTrue("(attachNum = " + ((Integer) attachNum).intValue() + ")",
				((Integer) attachNum).intValue() >= 0.0);
	}
}
