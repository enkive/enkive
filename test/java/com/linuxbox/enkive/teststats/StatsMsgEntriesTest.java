package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
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

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
import com.linuxbox.enkive.statistics.KeyConsolidationHandler;
import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.gathering.StatsMsgSearchGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.MongoGathererMessageSearchService;
import com.mongodb.Mongo;

public class StatsMsgEntriesTest {

	private static StatsMsgSearchGatherer msgEntries;
	private static Map<String, Object> stats;
	private static String name = "MsgEntriesGatherer";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		List<String> keys = new LinkedList<String>();
		keys.add("numMsg:avg,max,min:Number of Messages:messages");
		msgEntries = new StatsMsgSearchGatherer(name, "Message Statistics", "0 * * * * ?", keys);
		MongoMessageSearchService searchService;
		searchService = new MongoMessageSearchService(new Mongo(),
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		searchService.setDocSearchService(new IndriDocSearchQueryService());

		msgEntries.setSearchService(searchService);
		RawStats rawStats = msgEntries.getStatistics();
		stats = rawStats.getStatsMap();
		stats.put(STAT_TIMESTAMP, rawStats.getTimestamp());
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
		for (KeyConsolidationHandler key : msgEntries.getAttributes().getKeys()) {
			LinkedList<String> path = key.getKey();
			assertTrue("the format is incorrect for path: " + path,
					checkFormat(stats, path));
		}
	}

	@Test
	public void keyCountMatches() {
		int numKeys = stats.keySet().size();
		assertTrue("numKeys doesn't match: numKeys = " + numKeys, numKeys == 2);
	}

	@Test
	public void hasServiceName() {
		String sn = (String) msgEntries.getAttributes().getName();
		assertNotNull("no service name found in hasServiceName()", sn);
		assertTrue(sn.equals(name));
	}

	@Test
	public void hasTimeStamp() {
		Long time = ((Date) stats.get(STAT_TIMESTAMP)).getTime();
		assertTrue("runtime test exception in hasTimeStamp(): time = " + time,
				time != null);
	}

	@Test
	public void timeGTZero() {
		Long time = ((Date) stats.get(STAT_TIMESTAMP)).getTime();
		assertTrue("runtime test exception in timeGTZero(): time = " + time,
				time > 0);
	}

	@Test
	public void GTEZerotest() {
		Date startDate = new Date(0L);
		Date endDate = new Date();
		Map<String, Object> numEntriesStats = null;
		try{
			RawStats rawStatsOnDate = msgEntries.getStatistics(
					startDate, endDate);
			
			numEntriesStats = rawStatsOnDate.getStatsMap();
			numEntriesStats.put(STAT_TIMESTAMP, rawStatsOnDate.getTimestamp());
		} catch(Exception e) {
			assertTrue("numEntriesCrashed", false);
		}
		int numEntries = (Integer) numEntriesStats.get(STAT_NUM_ENTRIES);
		assertTrue("numEntries = " + numEntries, numEntries >= 0);
	}
}
