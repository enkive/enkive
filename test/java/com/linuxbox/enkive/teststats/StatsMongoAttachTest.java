package com.linuxbox.enkive.teststats;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoAttachmentsGatherer;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.linuxbox.enkive.TestingConstants;

public class StatsMongoAttachTest {
	protected static StatsMongoAttachmentsGatherer attach;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		attach = new StatsMongoAttachmentsGatherer(new Mongo(), TestingConstants.MONGODB_TEST_DATABASE, TestingConstants.MONGODB_TEST_FSFILES_COLLECTION, "AttachmentGatherer", "0 * * * * ?", false);
		attach.setUpper(new Date());
		attach.setLower(new Date(0L));
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
	public void testAvgNonZero() throws UnknownHostException, MongoException {
		double avg = attach.getAvgAttachSize();
		assertTrue("(avg = " + avg + ")", avg != 0.0);
	}
	
	@Test
	public void testMaxNonZero() throws UnknownHostException, MongoException {
		double max = attach.getMaxAttachSize();
		assertTrue("(max = " + max + ")", max != 0.0);
	}
	
	@Test
	public void testAvgGTZero() throws UnknownHostException, MongoException {
		double avg = attach.getAvgAttachSize();
		assertTrue("(avg = " + avg + ")", avg > 0.0);
	}
	
	@Test
	public void testMaxGTZero() throws UnknownHostException, MongoException {
		long max = attach.getMaxAttachSize();
		assertTrue("(max = " + max + ")", max > 0);
	}
	
	@Test
	public void testAvgLTEMax() throws UnknownHostException, MongoException {
		double avg = attach.getAvgAttachSize();
		long max = attach.getMaxAttachSize(); 
		assertTrue(" (avg = " + avg + ":max = " + max + ")", avg <= max);
	}
	
	@Test
	public void testERR() throws UnknownHostException, MongoException {
		attach = new StatsMongoAttachmentsGatherer(new Mongo(), TestingConstants.MONGODB_TEST_DATABASE, "IHopeThIsMess3s1tUp!", "AttachmentGatherer", "0 * * * * ?");
		attach.setUpper(new Date());
		attach.setLower(new Date(0L));
		double max = attach.getMaxAttachSize();
		double avg = attach.getAvgAttachSize();
		assertTrue("(avg = " + avg + ")", avg == -1);
		assertTrue("(max = " + max + ")", max == -1);
		Map<String, Object> stats = attach.getStatistics();
		assertNull("in attach.getStatistics() " + stats + " is not null", stats);
	}
}
