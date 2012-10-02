package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FREE_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INTERVAL;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_MAX_MEMORY;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_POINT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_PROCESSORS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MEMORY;
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
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
@SuppressWarnings("unchecked")
public class StatsRuntimeTest {
	private final static String serviceName = "RuntimeGatherer";
	protected static StatsRuntimeGatherer rtStat = null;
	protected static Map<String, Object> stats;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		List<String> keys = new LinkedList<String>();
		keys.add("freeM:avg,max,min:Free Memory:bytes:point");
		keys.add("maxM:avg,max,min:Max Memory:bytes:point");
		keys.add("totM:avg,max,min:Total Memory:bytes:point");
		keys.add("cores:avg,max,min:Processors::point");
		rtStat = new StatsRuntimeGatherer(serviceName, "Runtime Statistics", keys);
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

	public boolean checkFormat(Map<String, Object> stats,
			LinkedList<String> path) {
		if(stats == null || path.isEmpty()){
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
			if(path.contains(STAT_GATHERER_NAME) || path.contains(STAT_TIMESTAMP)){
				continue;
			}
			if(key.isPoint()){
				assertTrue("the format is incorrect for path: " + path, checkFormat((Map<String,Object>)stats.get(STAT_POINT), path));
			} else {
				assertTrue("the format is incorrect for path: " + path, checkFormat((Map<String,Object>)stats.get(STAT_INTERVAL), path));
			}
		}
	}

	@Test
	public void pointKeyCountMatches() {
		int numKeys = 0;
		int pointKeyCount = 0;
		for(ConsolidationKeyHandler key : rtStat.getAttributes().getKeys()){
			if(key.isPoint() && key.getMethods() != null){
				pointKeyCount++;
			}
		}
		
		if(stats.containsKey(STAT_POINT)){
			Map<String, Object> pointStats = (Map<String,Object>)stats.get(STAT_POINT);
			numKeys += pointStats.keySet().size();
		}
		
		assertTrue("numKeys doesn't match: numKeys = " + numKeys, numKeys == pointKeyCount);
	}
	
	@Test
	public void intervalKeyCountMatches() {
		int numKeys = 0;
		int intervalKeyCount = 0;
		for(ConsolidationKeyHandler key : rtStat.getAttributes().getKeys()){
			if(!key.isPoint() && key.getMethods() != null){
				System.out.println("Human Name: " + key.getHumanKey());
				intervalKeyCount++;
			}
		}
		
		if(stats.containsKey(STAT_INTERVAL)){
			Map<String, Object> pointStats = (Map<String,Object>)stats.get(STAT_INTERVAL);
			numKeys += pointStats.keySet().size();
		}
		
		assertTrue("numKeys doesn't match: numKeys = " + numKeys, numKeys == intervalKeyCount);
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
		Date date = ((Date) time.get(CONSOLIDATION_MAX));
		assertTrue("runtime test exception in timeGTZero(): date = " + date,
				date.getTime() > 0);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void lowerTimeGTZero() {
		Map<String, Object> time = (Map<String,Object>)stats.get(STAT_TIMESTAMP);
		Date date = ((Date) time.get(CONSOLIDATION_MIN));
		assertTrue("runtime test exception in timeGTZero(): date = " + date,
				date.getTime() > 0);
	}

	@Test
	public void maxMem() {
		Map<String, Object> ptStats = (Map<String, Object>)stats.get(STAT_POINT);
		long memory = ((Long) ptStats.get(STAT_MAX_MEMORY)).longValue();
		assertTrue("runtime test exception in maxMem(): memory = " + memory,
				memory > 0);
	}

	@Test
	public void freeMem() {
		Map<String, Object> ptStats = (Map<String, Object>)stats.get(STAT_POINT);
		long memory = ((Long) ptStats.get(STAT_FREE_MEMORY)).longValue();
		assertTrue("runtime test exception in freeMem(): memory = " + memory,
				memory > 0);
	}

	@Test
	public void totalMem() {
		Map<String, Object> ptStats = (Map<String, Object>)stats.get(STAT_POINT);
		long memory = ((Long) ptStats.get(STAT_TOTAL_MEMORY)).longValue();
		assertTrue("runtime test exception in totalMem(): memory = " + memory,
				memory > 0);
	}

	@Test
	public void processors() {
		Map<String, Object> ptStats = (Map<String, Object>)stats.get(STAT_POINT);
		int numProcessors = ((Integer) ptStats.get(STAT_PROCESSORS)).intValue();
		assertTrue("runtime test exception in processors(): numProcessors = "
				+ numProcessors, numProcessors > 0);
	}
}
