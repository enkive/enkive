/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
 * 
 * This file is part of Enkive CE (Community Edition).
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_COLLECTION;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
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
import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.gathering.Gatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.StatsMessageGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.MongoGathererMessageSearchService;
import com.linuxbox.enkive.statistics.gathering.mongodb.MongoStatsGridAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.MongoStatsCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.MongoStatsDatabaseGatherer;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.linuxbox.enkive.statistics.services.retrieval.StatsRetrievalException;
import com.linuxbox.enkive.statistics.services.retrieval.mongodb.MongoStatsRetrievalService;
import com.linuxbox.enkive.statistics.services.storage.mongodb.MongoStatsStorageService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

@RunWith(value = Parameterized.class)
public class StatsMongoStorageAndRetrievalTest {

	private static MongoStatsRetrievalService retrievalTester;
	private static MongoStatsStorageService storageTester;
	private static StatsGathererService gatherTester;
	private static StatsClient client;
	private static MongoClient m;
	private static Map<String, Object> stats;
	private static DB db;
	private static DBCollection coll;

	public StatsMongoStorageAndRetrievalTest(HashMap<String, Gatherer> map)
			throws GathererException, SchedulerException, ParseException {
		retrievalTester = new MongoStatsRetrievalService(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_COLL);
		storageTester = new MongoStatsStorageService(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_COLL);
		gatherTester = new StatsGathererService(map);
		client = new StatsClient(gatherTester, storageTester, retrievalTester);
		RawStats rawStats = gatherTester.gatherStats().iterator().next();
		stats = rawStats.toMap();
	}

	@Parameters
	public static Collection<Object[]> data() throws GathererException {
		try {
			m = new MongoClient();
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
		keys.add("db::Database Name::");
		keys.add("numObj:avg,max,min:Number of Objects::point");
		keys.add("nColls:avg,max,min:Number of Collections::point");
		keys.add("avgOSz:avg,max,min:Average Object Size:bytes:point");
		keys.add("dataSz:avg,max,min:Data Size:bytes:point");
		keys.add("totSz:avg,max,min:Total Size:bytes:point");
		keys.add("numInd:avg,max,min:Number of Indexes::point");
		keys.add("indSz:avg,max,min:Index Size:objects:point");
		keys.add("numExt:avg,max,min:Number of Extents::point");
		keys.add("fileSz:avg,max,min:File Size:bytes:point");
		Gatherer dbProp = new MongoStatsDatabaseGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE, "DBGatherer",
				"Database Statistics", keys);

		keys = new LinkedList<String>();
		keys.add("*.ns::Namespace::");
		keys.add("*.numObj:avg,max,min:Number of Objects::point");
		keys.add("*.avgOSz:avg,max,min:Average Object Size:bytes:point");
		keys.add("*.dataSz:avg,max,min:Data Size:bytes:point");
		keys.add("*.totSz:avg,max,min:Total Size:bytes:point");
		keys.add("*.numExt:avg,max,min:Number of Extents::point");
		keys.add("*.lExSz:avg,max,min:Last Extent Size:bytes:point");
		keys.add("*.numInd:avg,max,min:Number of Indexes::point");
		keys.add("*.indSz:avg,max,min:Index Size:objects:point");
		keys.add("*.indSzs.*:avg,max,min:Index Sizes:objects:point");
		Gatherer collProp = new MongoStatsCollectionGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE, "CollGatherer",
				"Collection Statistics", keys);

		keys = new LinkedList<String>();
		keys.add("freeM:avg,max,min:Free Memory:bytes:point");
		keys.add("maxM:avg,max,min:Max Memory:bytes:point");
		keys.add("totM:avg,max,min:Total Memory:bytes:point");
		keys.add("cores:avg,max,min:Processors::point");
		Gatherer runProp = new StatsRuntimeGatherer("RuntimeGatherer",
				"Runtime Statistics", keys);

		keys = new LinkedList<String>();
		keys.add("numMsg:avg:Number of Messages::interval");
		keys.add("totMsg:avg:Total Number of Messages::point");
		StatsMessageGatherer msgProp = new StatsMessageGatherer("MsgPropGatherer",
				"Message Statistics", keys);
		MongoGathererMessageSearchService searchService = null;

		try {
			searchService = new MongoGathererMessageSearchService(new MongoClient(),
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
		keys.add("avgAtt:avg:Average Attachments:number of attachments:interval");
		keys.add("maxAtt:max:Maximum Attachments:number of attachments:interval");
		MongoStatsGridAttachmentsGatherer attProp = new MongoStatsGridAttachmentsGatherer(
				m, TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_DOCUMENTS_COLLECTION,
				"AttachmentGatherer", "Attachment Statistics", keys);

		HashMap<String, Gatherer> map1 = new HashMap<String, Gatherer>();
		HashMap<String, Gatherer> map2 = new HashMap<String, Gatherer>();
		HashMap<String, Gatherer> map3 = new HashMap<String, Gatherer>();
		HashMap<String, Gatherer> map4 = new HashMap<String, Gatherer>();
		HashMap<String, Gatherer> map5 = new HashMap<String, Gatherer>();

		map1.put("DatabaseStatsService", dbProp);
		map2.put("CollStatsService", collProp);
		map3.put("RuntimeStatsService", runProp);
		map4.put("MsgEntriesStatsService", msgProp);
		map5.put("AttachstatsService", attProp);

		Object[][] data = new Object[][] { { map1 }, { map2 }, { map3 },
				{ map4 }, { map5 } };
		return Arrays.asList(data);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		client.storeRawStatsData(gatherTester.gatherStats());
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
				if (key.equals(STAT_TIMESTAMP)) {
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
