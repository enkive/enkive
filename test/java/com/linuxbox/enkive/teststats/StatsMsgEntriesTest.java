package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;
import com.linuxbox.enkive.statistics.gathering.MsgEntriesGatherer;
import com.mongodb.Mongo;

public class StatsMsgEntriesTest {

	private static MsgEntriesGatherer msgEntries;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		msgEntries = new MsgEntriesGatherer();
		MongoMessageSearchService searchService;
		searchService = new MongoMessageSearchService(new Mongo(),
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		searchService.setDocSearchService(new IndriDocSearchQueryService());

		msgEntries.setSearchService(searchService);
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

	@Test
	public void GTZerotest() {
		Date startDate = new Date(0L);
		Date endDate = new Date(System.currentTimeMillis());
		Map<String, Object> numEntriesStats = msgEntries.getStatistics(
				startDate, endDate);
		int numEntries = (Integer)numEntriesStats.get(STAT_NUM_ENTRIES);
		assertTrue("numEntries = " + numEntries, numEntries > 0);
	}
}
