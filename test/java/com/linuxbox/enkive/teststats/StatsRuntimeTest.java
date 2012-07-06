package com.linuxbox.enkive.teststats;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.statistics.KeyDef;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;

import static com.linuxbox.enkive.statistics.StatsConstants.*;

public class StatsRuntimeTest {
	private final static String serviceName = "RuntimeGatherer";
	protected static StatsRuntimeGatherer rtStat = new StatsRuntimeGatherer(serviceName, "* * * * * ?");
	protected static Map<String, Object> stats;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		stats = rtStat.getStatistics();
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
	public boolean checkFormat(Map<String, Object> stats, LinkedList<String> path){
		if(path.contains(STAT_SERVICE_NAME)){
			return true;
		}
		
		if(path.isEmpty()){
			return false;
		}
		String key = path.getFirst();
		if(path.size() == 1){
			if(key.equals("*"))
				return stats != null;
			else {
				return stats.get(key) != null; 
			}
		}
		
		boolean result = false;
		if (key.equals("*")) {
			path.removeFirst();
			for(String statKey: stats.keySet()){
				if(!(stats.get(statKey) instanceof Map)){
					result = path.size() == 1;
				} else {
					result = checkFormat((Map<String, Object>)stats.get(statKey), path);
				}
				if(result){
					break;
				}
			}
			path.addFirst(key);
			return result;
		} else if (stats.containsKey(key)) {
			path.removeFirst();
			result = checkFormat((Map<String, Object>)stats.get(key), path);
			path.addFirst(key);
			return result;
		}
		return false;
	}

	@Test
	public void testAttributes(){
		for(KeyDef key: rtStat.getAttributes().getKeys()){
			LinkedList<String> path = key.getKey();
			assertTrue("the format is incorrect for path: " + path,checkFormat(stats, path));
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
