package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_FILE_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_COLLECTIONS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_INDEX;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_OBJS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE_DB;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import com.linuxbox.enkive.TestingConstants;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoDBGatherer;
import com.mongodb.BasicDBObject;
import com.mongodb.Mongo;

public class StatsMongoDBTest {
	private static StatsMongoDBGatherer dbStats;
	private static BasicDBObject allStats;
	private static Mongo m;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		m = new Mongo();
		dbStats = new StatsMongoDBGatherer(m, TestingConstants.MONGODB_TEST_DATABASE, "DBGatherer", "0 * * * * ?");
		allStats = dbStats.getStats();
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
	public void typeTest() {
		String type = (String) allStats.get(STAT_TYPE);
		assertNotNull("in " + TestingConstants.MONGODB_TEST_DATABASE + " (type = null)", type);
		assertTrue("in " + TestingConstants.MONGODB_TEST_DATABASE + " (type = " + type + ")",
				type.compareTo(STAT_TYPE_DB) == 0);
	}

	@Test
	public void nameTest() {
		String name = (String) allStats.get(STAT_NAME);
		assertNotNull("in " + TestingConstants.MONGODB_TEST_DATABASE + " (type = null)", name);
		assertTrue("in " + TestingConstants.MONGODB_TEST_DATABASE + " (type = " + name + ")",
				name.compareTo(TestingConstants.MONGODB_TEST_DATABASE) == 0);
	}

	// GT means 'greater than'
	@Test
	public void numCollsGTZero() {
		assertNotNull("in " + TestingConstants.MONGODB_TEST_DATABASE + " (numColls = null)",
				allStats.get(STAT_NUM_COLLECTIONS));
		int numColls = ((Integer) allStats.get(STAT_NUM_COLLECTIONS))
				.intValue();
		assertTrue("in " + TestingConstants.MONGODB_TEST_DATABASE + " (numColls = " + numColls + ") ",
				numColls > 0);
	}

	@Test
	public void numObjsGTZero() {
		assertNotNull("in " + TestingConstants.MONGODB_TEST_DATABASE + " (numObjs = null)",
				allStats.get(STAT_NUM_OBJS));
		int numObjs = ((Integer) allStats.get(STAT_NUM_OBJS)).intValue();
		assertTrue("in " + TestingConstants.MONGODB_TEST_DATABASE + " (numObjs = " + numObjs + ") ",
				numObjs > 0);
	}

	@Test
	public void numAvgObjSizeGTZero() {
		assertNotNull("in " + TestingConstants.MONGODB_TEST_DATABASE + " (avgObjSize = null)",
				allStats.get(STAT_AVG_OBJ_SIZE));
		double avgObjSize = (Double) ((Double) allStats.get(STAT_AVG_OBJ_SIZE))
				.doubleValue();
		assertTrue(
				"in " + TestingConstants.MONGODB_TEST_DATABASE + " (avgObjSize = " + avgObjSize + ") ",
				avgObjSize > 0);
	}

	@Test
	public void dataGTZero() {
		assertNotNull("in " + TestingConstants.MONGODB_TEST_DATABASE + " (data = null)",
				allStats.get(STAT_DATA_SIZE));
		int data = ((Integer) allStats.get(STAT_DATA_SIZE)).intValue();
		assertTrue("in " + TestingConstants.MONGODB_TEST_DATABASE + " (data = " + data + ") ", data > 0);
	}

	@Test
	public void storageGTZero() {
		assertNotNull("in " + TestingConstants.MONGODB_TEST_DATABASE + " (storage = null)",
				allStats.get(STAT_TOTAL_SIZE));
		int storage = ((Integer) allStats.get(STAT_TOTAL_SIZE)).intValue();
		assertTrue("in " + TestingConstants.MONGODB_TEST_DATABASE + " (storage = " + storage + ") ",
				storage > 0);
	}

	@Test
	public void numIndexGTZero() {
		assertNotNull("in " + TestingConstants.MONGODB_TEST_DATABASE + " (numIndex = null)",
				allStats.get(STAT_NUM_INDEX));
		int numIndex = ((Integer) allStats.get(STAT_NUM_INDEX)).intValue();
		assertTrue("in " + TestingConstants.MONGODB_TEST_DATABASE + " (numIndex = " + numIndex + ") ",
				numIndex > 0);
	}

	@Test
	public void totalIndexSizeGTZero() {
		assertNotNull("in " + TestingConstants.MONGODB_TEST_DATABASE + " (totalIndexSize = null)",
				allStats.get(STAT_TOTAL_INDEX_SIZE));
		int totalIndexSize = ((Integer) allStats.get(STAT_TOTAL_INDEX_SIZE))
				.intValue();
		assertTrue("in " + TestingConstants.MONGODB_TEST_DATABASE + " (totalIndexSize = "
				+ totalIndexSize + ") ", totalIndexSize > 0);
	}

	@Test
	public void numExtentGTZero() {
		assertNotNull("in " + TestingConstants.MONGODB_TEST_DATABASE + " (numExtents = null)",
				allStats.get(STAT_NUM_EXTENT));
		int numExtents = ((Integer) allStats.get(STAT_NUM_EXTENT)).intValue();
		assertTrue(
				"in " + TestingConstants.MONGODB_TEST_DATABASE + " (numExtents = " + numExtents + ") ",
				numExtents > 0);
	}

	@Test
	public void fileSizeGTZero() {
		assertNotNull("in " + TestingConstants.MONGODB_TEST_DATABASE + " (fileSize = null)",
				allStats.get(STAT_FILE_SIZE));
		int fileSize = ((Integer) allStats.get(STAT_FILE_SIZE)).intValue();
		assertTrue("in " + TestingConstants.MONGODB_TEST_DATABASE + " (fileSize = " + fileSize + ") ",
				fileSize > 0);
	}
}
