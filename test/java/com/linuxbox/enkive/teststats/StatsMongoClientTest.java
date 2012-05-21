package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.*;
import static org.junit.Assert.assertTrue;
import com.linuxbox.enkive.TestingConstants;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;
import com.linuxbox.enkive.statistics.StatsClient;
import com.linuxbox.enkive.statistics.gathering.MongoMessageStatisticsService;
import com.linuxbox.enkive.statistics.gathering.StatsGatherException;
import com.linuxbox.enkive.statistics.gathering.StatsGatherService;
import com.linuxbox.enkive.statistics.gathering.StatsGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoAttachments;
import com.linuxbox.enkive.statistics.gathering.StatsMongoCollectionProperties;
import com.linuxbox.enkive.statistics.gathering.StatsMongoDBProperties;
import com.linuxbox.enkive.statistics.gathering.MsgEntriesGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeProperties;
import com.linuxbox.enkive.statistics.retrieval.MongoStatsRetrievalService;
import com.linuxbox.enkive.statistics.retrieval.StatsRetrievalException;
import com.linuxbox.enkive.statistics.storage.mongodb.MongoStatsStorageService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

@RunWith(value = Parameterized.class)
public class StatsMongoClientTest {
	private static StatsClient client;
	
	private static MongoStatsRetrievalService retrievalTester;
	private static MongoStatsStorageService storageTester;
	private static StatsGatherService gatherTester;
	private static Mongo m;
	private static Map<String, Object> stats;
	private static DB db;
	private static DBCollection coll;
	private static String statTypeName;
	
	public StatsMongoClientTest(HashMap<String, StatsGatherer> map) throws StatsGatherException {
		retrievalTester = new MongoStatsRetrievalService(m, TestingConstants.MONGODB_TEST_DATABASE);
		storageTester = new MongoStatsStorageService(m, TestingConstants.MONGODB_TEST_DATABASE);
		gatherTester = new StatsGatherService(map);
		client = new StatsClient(gatherTester, storageTester, retrievalTester);
		
		statTypeName = map.keySet().iterator().next();
		stats = new HashMap<String, Object>();
		stats.put(statTypeName, map.get(statTypeName).getStatistics());
	}

	@Parameters
	public static Collection<Object[]> data() throws UnknownHostException, MongoException {		
		try {
			m = new Mongo();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (MongoException e) {
			e.printStackTrace();
			System.exit(0);
		}
		db = m.getDB(TestingConstants.MONGODB_TEST_DATABASE);
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
		
		StatsGatherer dbProp = new StatsMongoDBProperties(m, TestingConstants.MONGODB_TEST_DATABASE);
		StatsGatherer collProp = new StatsMongoCollectionProperties(m, TestingConstants.MONGODB_TEST_DATABASE);
		StatsGatherer runProp = new StatsRuntimeProperties();
		MsgEntriesGatherer msgProp = new MsgEntriesGatherer();
		MongoMessageSearchService searchService;
		searchService = new MongoMessageSearchService(new Mongo(),
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		searchService.setDocSearchService(new IndriDocSearchQueryService());
		msgProp.setSearchService(searchService);
		StatsGatherer attProp = new StatsMongoAttachments(m, TestingConstants.MONGODB_TEST_DATABASE, TestingConstants.MONGODB_TEST_DOCUMENTS_COLLECTION);
		StatsGatherer msgStatProp = new MongoMessageStatisticsService(m, TestingConstants.MONGODB_TEST_DATABASE, TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		
		HashMap<String, StatsGatherer> map1 = new HashMap<String, StatsGatherer>();
		HashMap<String, StatsGatherer> map2 = new HashMap<String, StatsGatherer>();
		HashMap<String, StatsGatherer> map3 = new HashMap<String, StatsGatherer>();
		HashMap<String, StatsGatherer> map4 = new HashMap<String, StatsGatherer>();
		HashMap<String, StatsGatherer> map5 = new HashMap<String, StatsGatherer>();
		HashMap<String, StatsGatherer> map6 = new HashMap<String, StatsGatherer>();
		
		map1.put("DatabaseStatsService", dbProp);
		map2.put("CollStatsService", collProp);
		map3.put("RuntimeStatsService", runProp);
		map4.put("MsgEntriesStatsService", msgProp);
		map5.put("AttachstatsService", attProp);
		map6.put("msgStatStatsService", msgStatProp);
	
		Object[][] data = new Object[][] { { map1 }, { map2 }, { map3 }, {map4}, {map5}, {map6}};
		return Arrays.asList(data);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		client.storeData(client.gatherData(null));
	}

	@After
	public void tearDown() throws Exception {
		coll.drop();
	}	
	
	
	@Test
	public void hasAllKeysTest() throws StatsRetrievalException {
		Set<Map<String, Object>> dataSet  = client.retrieveData(null, null, null);
		
		boolean areEqual = true;
		String errorString = "The following keys are missing: ";
		for(Map<String, Object> temp: dataSet){
			for(String key : temp.keySet()){
				if(!temp.containsKey(key)){
					errorString = errorString + " " + key;
				}
			}
		}
		assertTrue(errorString,areEqual);
	}
}