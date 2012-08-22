package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_PROCESSORS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MEMORY;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
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
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;

public class StatsRuntimeTest {
	private final static String serviceName = "RuntimeGatherer";
	protected static StatsRuntimeGatherer rtStat = null;
	protected static Map<String, Object> stats;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		List<String> keys = new LinkedList<String>();
		keys.add("freeM:avg,max,min:Free Memory:bytes");
		keys.add("maxM:avg,max,min:Max Memory:bytes");
		keys.add("totM:avg,max,min:Total Memory:bytes");
		keys.add("cores:avg,max,min:Processors:");
		rtStat = new StatsRuntimeGatherer(
				serviceName, "Runtime Statistics", "0 * * * * ?", keys);
		RawStats rawStats = rtStat.getStatistics();
		stats = rawStats.toMap();
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
		for (ConsolidationKeyHandler key : rtStat.getAttributes().getKeys()) {
			LinkedList<String> path = key.getKey();
			assertTrue("the format is incorrect for path: " + path,
					checkFormat(stats, path));
		}
	}

	@Test
	public void keyCountMatches() {
		int numKeys = stats.keySet().size();
		assertTrue("numKeys doesn't match: numKeys = " + numKeys, numKeys == 5);
	}

	@Test
	public void hasServiceName() {
		String sn = (String) rtStat.getAttributes().getName();
		assertNotNull("no service name found in hasServiceName()", sn);
		assertTrue(sn.equals(serviceName));
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
	public void maxMem() {
		long memory = ((Long) stats.get(STAT_MAX_MEMORY)).longValue();
		assertTrue("runtime test exception in maxMem(): memory = " + memory,
				memory > 0);
	}

	@Test
	public void freeMem() {
		long memory = ((Long) stats.get(STAT_FREE_MEMORY)).longValue();
		assertTrue("runtime test exception in freeMem(): memory = " + memory,
				memory > 0);
	}

	@Test
	public void totalMem() {
		long memory = ((Long) stats.get(STAT_TOTAL_MEMORY)).longValue();
		assertTrue("runtime test exception in totalMem(): memory = " + memory,
				memory > 0);
	}

	@Test
	public void processors() {
		int numProcessors = ((Integer) stats.get(STAT_PROCESSORS)).intValue();
		assertTrue("runtime test exception in processors(): numProcessors = "
				+ numProcessors, numProcessors > 0);
	}
}
