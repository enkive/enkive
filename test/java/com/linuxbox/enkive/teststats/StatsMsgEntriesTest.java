package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;
import com.linuxbox.enkive.statistics.KeyDef;
import com.linuxbox.enkive.statistics.gathering.StatsMsgSearchGatherer;
import com.mongodb.Mongo;

public class StatsMsgEntriesTest {

	private static StatsMsgSearchGatherer msgEntries;
	private static Map<String, Object> stats;
	private static String name = "MsgEntriesGatherer";
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		msgEntries = new StatsMsgSearchGatherer(name, "0 * * * * ?");
		MongoMessageSearchService searchService;
		searchService = new MongoMessageSearchService(new Mongo(),
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		searchService.setDocSearchService(new IndriDocSearchQueryService());

		msgEntries.setSearchService(searchService);
		stats = msgEntries.getStatistics();
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
		for(KeyDef key: msgEntries.getAttributes().getKeys()){
			LinkedList<String> path = key.getKey();
			assertTrue("the format is incorrect for path: " + path,checkFormat(stats, path));
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
	public void GTZerotest() {
		Date startDate = new Date(0L);
		Date endDate = new Date();
		Map<String, Object> numEntriesStats = msgEntries.getStatistics(
				startDate, endDate);
		int numEntries = (Integer)numEntriesStats.get(STAT_NUM_ENTRIES);
		assertTrue("numEntries = " + numEntries, numEntries > 0);
	}
}
