package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INTERVAL;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_POINT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
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

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;
import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.gathering.StatsMsgGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.MongoGathererMessageSearchService;
import com.mongodb.Mongo;
@SuppressWarnings("unchecked")
public class StatsMsgTest {

	private static StatsMsgGatherer msgEntries;
	private static Map<String, Object> stats;
	private static String name = "MsgEntriesGatherer";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		List<String> keys = new LinkedList<String>();
		keys.add("numMsg:avg:Number of Messages::interval");
		keys.add("totMsg:avg:Total Number of Messages::point");
		msgEntries = new StatsMsgGatherer(name, "Message Statistics", keys);
		MongoGathererMessageSearchService searchService;
		searchService = new MongoGathererMessageSearchService(new Mongo(),
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		searchService.setDocSearchService(new IndriDocSearchQueryService());

		msgEntries.setSearchService(searchService);
		RawStats rawStats = msgEntries.getStatistics();
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
	public void attributesNotNull() {
		assertTrue("msgEntries.getAttributes returned null", msgEntries.getAttributes() != null);
	}
	
	@Test
	public void testAttributes() {
		for (ConsolidationKeyHandler key : msgEntries.getAttributes().getKeys()) {
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
		for(ConsolidationKeyHandler key : msgEntries.getAttributes().getKeys()){
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
		for(ConsolidationKeyHandler key : msgEntries.getAttributes().getKeys()){
			if(!key.isPoint() && key.getMethods() != null){
				intervalKeyCount++;
			}
		}
		
		if(stats.containsKey(STAT_INTERVAL)){
			Map<String, Object> intervalStats = (Map<String,Object>)stats.get(STAT_INTERVAL);
			numKeys += intervalStats.keySet().size();
		}
		System.out.println("intervalKeyCount: " + intervalKeyCount);
		System.out.println("numKeys: " + numKeys);
		assertTrue("numKeys doesn't match: numKeys = " + numKeys, numKeys == intervalKeyCount);
	}

	@Test
	public void hasServiceName() {
		String sn = (String) msgEntries.getAttributes().getName();
		assertNotNull("no service name found in hasServiceName()", sn);
		assertTrue(sn.equals(name));
	}

	@Test
	public void hasTimeStamp() {
		Map<String, Object> time = (Map<String,Object>)stats.get(STAT_TIMESTAMP);
		assertTrue("runtime test exception in hasTimeStamp(): time = " + time,
				time != null);
	}

	@Test
	public void upperTimeGTZero() {
		Map<String, Object> time = (Map<String,Object>)stats.get(STAT_TIMESTAMP);
		Date date = ((Date) time.get(CONSOLIDATION_MAX));
		assertTrue("runtime test exception in timeGTZero(): date = " + date,
				date.getTime() > 0);
	}
	
	@Test
	public void lowerTimeGTZero() {
		Map<String, Object> time = (Map<String,Object>)stats.get(STAT_TIMESTAMP);
		Date date = ((Date) time.get(CONSOLIDATION_MIN));
		assertTrue("runtime test exception in timeGTZero(): date = " + date,
				date.getTime() > 0);
	}

	@Test
	public void GTEZerotest() {
		Date startDate = new Date(0L);
		Date endDate = new Date();
		Map<String, Object> numEntriesStats = null;
		try{
			RawStats rawStatsOnDate = msgEntries.getStatistics(
					startDate, endDate);
			
			numEntriesStats = rawStatsOnDate.toMap();
			numEntriesStats = (Map<String,Object>)numEntriesStats.get(STAT_INTERVAL);
		} catch(Exception e) {
			assertTrue("numEntriesCrashed", false);
		}
		int numEntries = (Integer) numEntriesStats.get(STAT_NUM_ENTRIES);
		assertTrue("numEntries = " + numEntries, numEntries >= 0);
	}
}
