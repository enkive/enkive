package com.linuxbox.enkive.teststats;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;
import com.linuxbox.enkive.statistics.StatsMsgEntries;
import com.mongodb.Mongo;

public class StatsMsgEntriesTest {
	
	private static StatsMsgEntries msgEntries;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		msgEntries = new StatsMsgEntries();
		
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
	public void notZerotest() {
		int numEntries = msgEntries.numEntries();
		assertTrue("numEntries = " + numEntries, numEntries != 0);
	}
	
	@Test
	public void GTZerotest() {
		int numEntries = msgEntries.numEntries();
		assertTrue("numEntries = " + numEntries, numEntries > 0);
	}

}
