package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_NUM;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_SIZE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
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

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.statistics.KeyConsolidationHandler;
import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoAttachmentsGatherer;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsMongoAttachTest {
	protected static StatsMongoAttachmentsGatherer attach;
	private static Map<String, Object> stats;
	private static String name = "AttachmentGatherer";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		List<String> keys = new LinkedList<String>();
		keys.add("attNum:avg:Number of Attachments:");
		keys.add("attSz:avg:Attachment Size:bytes");
		attach = new StatsMongoAttachmentsGatherer(new Mongo(),
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_FSFILES_COLLECTION, name, "Attachment Statistics",
				"0 * * * * ?", keys);
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

	@Test
	public void testAttributes() {
		for (KeyConsolidationHandler key : attach.getAttributes().getKeys()) {
			LinkedList<String> path = key.getKey();
			assertTrue("the format is incorrect for path: " + path,
					checkFormat(stats, path));
		}
	}

	@Test
	public void keyCountMatches() {
		int numKeys = stats.keySet().size();
		assertTrue("numKeys doesn't match: numKeys = " + numKeys, numKeys == 4);
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
		Map<String, Object> time = (Map<String,Object>)stats.get(STAT_TIMESTAMP);
		assertTrue("runtime test exception in hasTimeStamp(): time = " + time,
				time != null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void upperTimeGTZero() {
		Map<String, Object> time = (Map<String,Object>)stats.get(STAT_TIMESTAMP);
		Date date = ((Date) time.get(GRAIN_MAX));
		assertTrue("runtime test exception in timeGTZero(): date = " + date,
				date.getTime() > 0);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void lowerTimeGTZero() {
		Map<String, Object> time = (Map<String,Object>)stats.get(STAT_TIMESTAMP);
		Date date = ((Date) time.get(GRAIN_MIN));
		assertTrue("runtime test exception in timeGTZero(): date = " + date,
				date.getTime() > 0);
	}

	@Test
	public void testAttachSize() throws UnknownHostException, MongoException {
		Object attachSz = stats.get(STAT_ATTACH_SIZE);
		assertTrue("(max = " + attachSz + ")", ((Long)attachSz).longValue() >= 0.0);
	}

	@Test
	public void testAttNum() throws UnknownHostException, MongoException {
		Object attachNum = stats.get(STAT_ATTACH_NUM);
		assertTrue("(attachNum = " + ((Integer)attachNum).intValue() + ")", ((Integer)attachNum).intValue() >= 0.0);
	}
}
