package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_COLLECTION;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.quartz.SchedulerException;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.GathererInterface;
import com.linuxbox.enkive.statistics.gathering.StatsMsgSearchGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoDBGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoMsgGatherer;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.linuxbox.enkive.statistics.services.retrieval.StatsRetrievalException;
import com.linuxbox.enkive.statistics.services.retrieval.mongodb.MongoStatsRetrievalService;
import com.linuxbox.enkive.statistics.services.storage.mongodb.MongoStatsStorageService;
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

	public StatsMongoStorageAndRetrievalTest(
			HashMap<String, GathererInterface> map) throws GathererException,
			SchedulerException, ParseException {
		retrievalTester = new MongoStatsRetrievalService(m, 
				TestingConstants.MONGODB_TEST_DATABASE, TestingConstants.MONGODB_TEST_COLL);
		storageTester = new MongoStatsStorageService(m,
				TestingConstants.MONGODB_TEST_DATABASE, TestingConstants.MONGODB_TEST_COLL);
		gatherTester = new StatsGathererService(map);
		stats = (Map<String, Object>) gatherTester.gatherStats().iterator()
				.next();
	}

	@Parameters
	public static Collection<Object[]> data() throws GathererException {
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
		coll.drop();
		
		List<String> keys = new LinkedList<String>();
		keys.add("db:");
		keys.add("numObj:avg,max,min");
		keys.add("nColls:avg,max,min");
		keys.add("avgOSz:avg,max,min");
		keys.add("dataSz:avg,max,min");
		keys.add("totSz:avg,max,min");
		keys.add("numInd:avg,max,min");
		keys.add("indSz:avg,max,min");
		keys.add("numExt:avg,max,min");
		keys.add("fileSz:avg,max,min");
		GathererInterface dbProp = new StatsMongoDBGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE, "DBGatherer",
				"* * * * * ?", keys);
		
		keys = new LinkedList<String>();
		keys.add("*.ns:");
		keys.add("*.numObj:avg,max,min");
		keys.add("*.avgOSz:avg,max,min");
		keys.add("*.dataSz:avg,max,min");
		keys.add("*.totSz:avg,max,min");
		keys.add("*.numExt:avg,max,min");
		keys.add("*.lExSz:avg,max,min");
		keys.add("*.numInd:avg,max,min");
		keys.add("*.indSz:avg,max,min");
		keys.add("*.indSzs.*:avg,max,min");
		GathererInterface collProp = new StatsMongoCollectionGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE, "CollGatherer",
				"* * * * * ?", keys);
		
		keys = new LinkedList<String>();
		keys.add("freeM:avg,max,min");
		keys.add("maxM:avg,max,min");
		keys.add("totM:avg,max,min");
		keys.add("cores:avg,max,min");
		GathererInterface runProp = new StatsRuntimeGatherer("RuntimeGatherer",
				"* * * * * ?", keys);
		
		keys = new LinkedList<String>();
		keys.add("numMsg:avg,max,min");
		StatsMsgSearchGatherer msgProp = new StatsMsgSearchGatherer(
				"MsgPropGatherer", "* * * * * ?", keys);
		
		MongoMessageSearchService searchService = null;
		
		try {
			searchService = new MongoMessageSearchService(new Mongo(),
					TestingConstants.MONGODB_TEST_DATABASE,
					TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
		searchService.setDocSearchService(new IndriDocSearchQueryService());
		msgProp.setSearchService(searchService);
		
		keys = new LinkedList<String>();
		keys.add("avgAtt:avg");
		keys.add("maxAtt:max");
		StatsMongoAttachmentsGatherer attProp = new StatsMongoAttachmentsGatherer(
				m, TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_DOCUMENTS_COLLECTION,
				"AttachmentGatherer", "* * * * * ?", false, keys);
		attProp.setLowerDate(new Date(0L));
		attProp.setUpperDate(new Date());
		
		keys = new LinkedList<String>();
		keys.add("msgArchive:avg,max,min");
		GathererInterface msgStatProp = new StatsMongoMsgGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION,
				"MsgStatGatherer", "* * * * * ?", keys);

		HashMap<String, GathererInterface> map1 = new HashMap<String, GathererInterface>();
		HashMap<String, GathererInterface> map2 = new HashMap<String, GathererInterface>();
		HashMap<String, GathererInterface> map3 = new HashMap<String, GathererInterface>();
		HashMap<String, GathererInterface> map4 = new HashMap<String, GathererInterface>();
		HashMap<String, GathererInterface> map5 = new HashMap<String, GathererInterface>();
		HashMap<String, GathererInterface> map6 = new HashMap<String, GathererInterface>();

		map1.put("DatabaseStatsService", dbProp);
		map2.put("CollStatsService", collProp);
		map3.put("RuntimeStatsService", runProp);
		map4.put("MsgEntriesStatsService", msgProp);
		map5.put("AttachstatsService", attProp);
		map6.put("msgStatStatsService", msgStatProp);

		Object[][] data = new Object[][] { { map1 }, { map2 }, { map3 },
				{ map4 }, { map5 }, { map6 } };
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
		Set<Map<String, Object>> dataSet = retrievalTester.queryStatistics();
		boolean areEqual = true;
		String errorString = "The following keys are missing: ";
		for (String key : stats.keySet()) {
			for (Map<String, Object> temp : dataSet) {
				if (!temp.containsKey(key)) {
					areEqual = false;
					errorString = errorString + " " + key;
					break;
				}
			}
		}
		assertTrue(errorString, areEqual);
	}

	@Test
	public void hasAllValsTest() throws StatsRetrievalException {
		Set<Map<String, Object>> dataSet = retrievalTester.queryStatistics();
		boolean areEqual = true;
		String errorString = "The following keys have incorrect vals: ";
		for (Map<String, Object> temp : dataSet) {
			for (String key : stats.keySet()) {
				if (key.equals(STAT_TIME_STAMP)) {
					continue;
				}
				if (!stats.get(key).equals(temp.get(key))) {
					System.out.println(key + ": " + stats.get(key) + " vs. "
							+ temp.get(key));
					errorString = errorString + " " + key;
					areEqual = false;
				}
			}
		}
		assertTrue(errorString, areEqual);
	}
}