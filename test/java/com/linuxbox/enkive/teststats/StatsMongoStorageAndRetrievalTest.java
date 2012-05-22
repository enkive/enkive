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
import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoMsgGatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.StatsMongoAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoDBGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMsgSearchGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.linuxbox.enkive.statistics.retrieval.StatsRetrievalException;
import com.linuxbox.enkive.statistics.retrieval.mongodb.MongoStatsRetrievalService;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.linuxbox.enkive.statistics.storage.mongodb.MongoStatsStorageService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

@RunWith(value = Parameterized.class)
public class StatsMongoStorageAndRetrievalTest {
	
	private static MongoStatsRetrievalService retrievalTester;
	private static MongoStatsStorageService storageTester;
	private static StatsGathererService gatherTester;
	private static Mongo m;
	private static Map<String, Object> stats;
	private static DB db;
	private static DBCollection coll;
	private static String statTypeName;
	
	@SuppressWarnings("unchecked")
	public StatsMongoStorageAndRetrievalTest(HashMap<String, AbstractGatherer> map) throws GathererException {
		retrievalTester = new MongoStatsRetrievalService(m, TestingConstants.MONGODB_TEST_DATABASE);
		storageTester = new MongoStatsStorageService(m, TestingConstants.MONGODB_TEST_DATABASE);
		gatherTester = new StatsGathererService(map);
		
		statTypeName = map.keySet().iterator().next();
		stats = (Map<String, Object>) gatherTester.gatherStats().iterator().next().get(statTypeName);
		stats.put(STAT_SERVICE_NAME, statTypeName);
	}

	@Parameters
	public static Collection<Object[]> data() {		
		try {
			m = new Mongo();
		} catch (UnknownHostException e) {
			//TODO: write actual handling
			e.printStackTrace();
			System.exit(0);
		} catch (MongoException e) {
			e.printStackTrace();
			System.exit(0);
		}
		db = m.getDB(TestingConstants.MONGODB_TEST_DATABASE);
		coll = db.getCollection(STAT_STORAGE_COLLECTION);
		
		AbstractGatherer dbProp = new StatsMongoDBGatherer(m, TestingConstants.MONGODB_TEST_DATABASE);
		AbstractGatherer collProp = new StatsMongoCollectionGatherer(m, TestingConstants.MONGODB_TEST_DATABASE);
		AbstractGatherer runProp = new StatsRuntimeGatherer();
		StatsMsgSearchGatherer msgProp = new StatsMsgSearchGatherer();
		MongoMessageSearchService searchService = null;
		try {
			searchService = new MongoMessageSearchService(new Mongo(),
					TestingConstants.MONGODB_TEST_DATABASE,
					TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		searchService.setDocSearchService(new IndriDocSearchQueryService());
		msgProp.setSearchService(searchService);
		AbstractGatherer attProp = new StatsMongoAttachmentsGatherer(m, TestingConstants.MONGODB_TEST_DATABASE, TestingConstants.MONGODB_TEST_DOCUMENTS_COLLECTION);
		AbstractGatherer msgStatProp = new StatsMongoMsgGatherer(m, TestingConstants.MONGODB_TEST_DATABASE, TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		
		HashMap<String, AbstractGatherer> map1 = new HashMap<String, AbstractGatherer>();
		HashMap<String, AbstractGatherer> map2 = new HashMap<String, AbstractGatherer>();
		HashMap<String, AbstractGatherer> map3 = new HashMap<String, AbstractGatherer>();
		HashMap<String, AbstractGatherer> map4 = new HashMap<String, AbstractGatherer>();
		HashMap<String, AbstractGatherer> map5 = new HashMap<String, AbstractGatherer>();
		HashMap<String, AbstractGatherer> map6 = new HashMap<String, AbstractGatherer>();
		
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
		storageTester.storeStatistics(gatherTester.gatherStats());
	}

	@After
	public void tearDown() throws Exception {
		coll.drop();
	}	
	
	
	@Test
	public void hasAllKeysTest() throws StatsRetrievalException {
		Set<Map<String, Object>> dataSet  = retrievalTester.queryStatistics();
		System.out.println("dataSet: " + dataSet);
		System.out.println("stats: "  + stats);
		boolean areEqual = true;
		String errorString = "The following keys are missing: ";
		for(String key : stats.keySet()){
			for(Map<String, Object> temp: dataSet){
				if(!temp.containsKey(key)){
					areEqual = false;
					errorString = errorString + " " + key;
				}
			}
		}
		assertTrue(errorString,areEqual);
	}
	
	@Test
	public void hasAllValsTest() throws StatsRetrievalException {
		Set<Map<String, Object>> dataSet  = retrievalTester.queryStatistics();
		boolean areEqual = true;
		String errorString = "The following keys have incorrect vals: ";
		for(Map<String, Object> temp: dataSet){
			for(String key : stats.keySet()){
				if(key.equals(STAT_TIME_STAMP)){
					continue;
				}
				if(!stats.get(key).equals(temp.get(key))){
					System.out.println(stats.get(key) + " vs. " + temp.get(key));
					errorString = errorString + " " + key;
					areEqual = false;
				}
			}
		}
		assertTrue(errorString,areEqual);
	}
}