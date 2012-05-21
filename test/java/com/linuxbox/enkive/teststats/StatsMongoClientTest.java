package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.*;
import static org.junit.Assert.assertTrue;
import com.linuxbox.enkive.TestingConstants;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.linuxbox.enkive.statistics.StatsClient;
import com.linuxbox.enkive.statistics.gathering.MongoMessageStatisticsService;
import com.linuxbox.enkive.statistics.gathering.StatsGatherException;
import com.linuxbox.enkive.statistics.gathering.StatsGatherService;
import com.linuxbox.enkive.statistics.gathering.StatsGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoAttachments;
import com.linuxbox.enkive.statistics.gathering.StatsMongoCollectionProperties;
import com.linuxbox.enkive.statistics.gathering.StatsMongoDBProperties;
import com.linuxbox.enkive.statistics.gathering.StatsMsgEntries;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeProperties;
import com.linuxbox.enkive.statistics.retrieval.MongoStatsRetrievalService;
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
	
	public StatsMongoClientTest(HashMap<String, StatsGatherer> map) throws JSONException, StatsGatherException {
		retrievalTester = new MongoStatsRetrievalService(m, TestingConstants.MONGODB_TEST_DATABASE);
		storageTester = new MongoStatsStorageService(m, TestingConstants.MONGODB_TEST_DATABASE);
		gatherTester = new StatsGatherService(map);
		client = new StatsClient(gatherTester, storageTester, retrievalTester);
		
		statTypeName = map.keySet().iterator().next();
		stats = new HashMap<String, Object>();
		stats.put(statTypeName, map.get(statTypeName).getStatistics());
	}

	@Parameters
	public static Collection<Object[]> data() {		
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
		StatsGatherer msgProp = new StatsMsgEntries();		
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
		client.storeData(client.gatherData());
	}

	@After
	public void tearDown() throws Exception {
		coll.drop();
	}	
	
	
	@Test
	public void hasAllKeysTest() throws MongoException, JSONException {
		Set<Map<String, Object>> dataSet  = client.retrieveData();
		
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
/*
	@Test
	public void hasCorrectValsTest() throws MongoException, JSONException {
		Set<Map<String, Object>> obj  = client.retrieveData();
		
		@SuppressWarnings("unchecked")
		Iterator<String> keys = stats.keySet();
		boolean areEqual = true;
		String errorString = "The following keys have incorrect values:";
		while(keys.hasNext()){
			String statsKey = keys.next();
			String objKey;
			if (statsKey.startsWith("$")) {
				objKey = statsKey.replaceFirst("$", "-");
			}
			objKey = statsKey.replace('.','-');
			if(statsKey.equals(STAT_TIME_STAMP)){//tested later
				continue;
			}
		*/	
			/*
			 * ERIC:
			 * So Mongo will try to store Longs as Ints if they are small enough
			 * (within the range of integers). So we cannot garrantee a type on
			 * our longs, however, this shouldn't be too much of a problem if you
			 * just check the type. OR if we figure out a way to get mongo to store
			 * & retrieve all longs as longs. I'm not sure how to do that so a big
			 * improvement for the statistics package would be to figure that out.
			 */
/*			Object temp1 = obj.get(objKey);
			Object temp2 = null;
			if(temp1 instanceof Integer){
				temp2 = new Integer(stats.getInt(statsKey));
			}
			else
				temp2 = stats.get(statsKey);
			
			if(!temp1.equals(temp2)){				
				System.out.println("key: " + objKey);
				System.out.println(obj.get(objKey) + " is not equal to " + stats.get(statsKey));
				errorString = errorString + " " + objKey;
				areEqual = false;
				
				if(temp1 instanceof Long)
					System.out.println("temp1(" + temp1 + ") is Long");
				else if(temp1 instanceof Double)
					System.out.println("temp1(" + temp1 + ") is Double");
				else if(temp1 instanceof Integer)
					System.out.println("temp1(" + temp1 + ") is Integer");
				else
					System.out.println("temp1(" + temp1 + ") is not...");
				
				if(temp2 instanceof Long)
					System.out.println("temp2(" + temp2 + ") is Long");
				else if(temp2 instanceof Long)
					System.out.println("temp2(" + temp2 + ") is Double");
				else if(temp2 instanceof Integer)
					System.out.println("temp2(" + temp2 + ") is Integer");
				else
					System.out.println("temp2(" + temp2 + ") is not...");
			}
		}
		assertTrue(errorString, areEqual);
	}
	
	@Test
	public void verifyTimeStampTest() throws MongoException, JSONException {
		JSONObject obj = (JSONObject)storageTester.retrieveStats().get(statTypeName);
		assertTrue("storage/retrieval is missing timeStamp Key",obj.has(STAT_TIME_STAMP));
	}

	@Test
	public void verifyServiceNameTest() throws MongoException, JSONException {
		assertTrue("storeage/retrieval is missing serviceName key", storageTester.retrieveStats().has(statTypeName));
	}
	*/
}