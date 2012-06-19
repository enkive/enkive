package com.linuxbox.enkive.teststats;

import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.Date;

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
	/*
	private static String dbName = STAT_DB_NAME;
	private static String collName = STAT_FSFILE_NAME;//"fs.files";
	private static String chunkCollName = STAT_CHUNK_NAME;//fs.chunks";
	*/
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		attach = new StatsMongoAttachmentsGatherer(new Mongo(), TestingConstants.MONGODB_TEST_DATABASE, TestingConstants.MONGODB_TEST_FSFILES_COLLECTION);
		Date upper = new Date(2335451471025L);
		Date lower = new Date(0335451471025L);
		attach.setUpper(upper);
		attach.setLower(lower);
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
		assertTrue("in testAvgNonZero() (avg = " + avg + ")", avg != 0.0);
	}
	
	@Test
	public void testMaxNonZero() throws UnknownHostException, MongoException {
		double max = attach.getMaxAttachSize();
		assertTrue("in testMaxNonZero() (max = " + max + ")", max != 0.0);
	}
	
	@Test
	public void testAvgGTZero() throws UnknownHostException, MongoException {
		double avg = attach.getAvgAttachSize();
		assertTrue("in testAvgGTZero() (avg = " + avg + ")", avg > 0.0);
	}
	
	@Test
	public void testMaxGTZero() throws UnknownHostException, MongoException {
		long max = attach.getMaxAttachSize();
		assertTrue("in testMaxGTZero() (max = " + max + ")", max > 0);
	}
	
	@Test
	public void testAvgLTEMax() throws UnknownHostException, MongoException {
		double avg = attach.getAvgAttachSize();
		long max = attach.getMaxAttachSize(); 
		assertTrue("in testAvgLTEMax() (avg = " + avg + ":max = " + max + ")", avg <= max);
	}
}
