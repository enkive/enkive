package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.statistics.KeyDef;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoAttachmentsGatherer;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsMongoAttachTest {
	protected static StatsMongoAttachmentsGatherer attach;
	private static Map<String, Object> stats;
	private static String name = "AttachmentGatherer";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		attach = new StatsMongoAttachmentsGatherer(new Mongo(),
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_FSFILES_COLLECTION, name,
				"0 * * * * ?", false);
		attach.setUpper(new Date());
		attach.setLower(new Date(0L));
		stats = attach.getStatistics();
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
		if (path.contains(STAT_SERVICE_NAME)) {
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

	@Test
	public void testAttributes() {
		for (KeyDef key : attach.getAttributes().getKeys()) {
			LinkedList<String> path = key.getKey();
			assertTrue("the format is incorrect for path: " + path,
					checkFormat(stats, path));
		}
	}

	@Test
	public void keyCountMatches() {
		int numKeys = stats.keySet().size();
		assertTrue("numKeys doesn't match: numKeys = " + numKeys, numKeys == 3);
	}

	@Test
	public void hasServiceName() {
		String sn = (String) attach.getAttributes().getName();
		assertNotNull("no service name found in hasServiceName()", sn);
		assertTrue(sn.equals(name));
	}

	@Test
	public void hasTimeStamp() {
		Long time = ((Long) stats.get(STAT_TIME_STAMP));
		assertTrue("runtime test exception in hasTimeStamp(): time = " + time,
				time != null);
	}

	@Test
	public void timeGTZero() {
		Long time = ((Long) stats.get(STAT_TIME_STAMP));
		assertTrue("runtime test exception in timeGTZero(): time = " + time,
				time > 0);
	}

	@Test
	public void testAvgNonZero() throws UnknownHostException, MongoException {
		double avg = attach.getAvgAttachSize();
		assertTrue("(avg = " + avg + ")", avg != 0.0);
	}

	@Test
	public void testMaxNonZero() throws UnknownHostException, MongoException {
		double max = attach.getMaxAttachSize();
		assertTrue("(max = " + max + ")", max != 0.0);
	}

	@Test
	public void testAvgGTZero() throws UnknownHostException, MongoException {
		double avg = attach.getAvgAttachSize();
		assertTrue("(avg = " + avg + ")", avg > 0.0);
	}

	@Test
	public void testMaxGTZero() throws UnknownHostException, MongoException {
		long max = attach.getMaxAttachSize();
		assertTrue("(max = " + max + ")", max > 0);
	}

	@Test
	public void testAvgLTEMax() throws UnknownHostException, MongoException {
		double avg = attach.getAvgAttachSize();
		long max = attach.getMaxAttachSize();
		assertTrue(" (avg = " + avg + ":max = " + max + ")", avg <= max);
	}

	@Test
	public void testERR() throws UnknownHostException, MongoException {
		attach = new StatsMongoAttachmentsGatherer(new Mongo(),
				TestingConstants.MONGODB_TEST_DATABASE, "IHopeThIsMess3s1tUp!",
				"AttachmentGatherer", "0 * * * * ?");
		attach.setUpper(new Date());
		attach.setLower(new Date(0L));
		double max = attach.getMaxAttachSize();
		double avg = attach.getAvgAttachSize();
		assertTrue("(avg = " + avg + ")", avg == -1);
		assertTrue("(max = " + max + ")", max == -1);
		Map<String, Object> stats = attach.getStatistics();
		assertNull("in attach.getStatistics() " + stats + " is not null", stats);
	}
}
