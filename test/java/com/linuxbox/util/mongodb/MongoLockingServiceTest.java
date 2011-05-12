package com.linuxbox.util.mongodb;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.util.lockservice.LockReleaseException;
import com.linuxbox.util.lockservice.mongodb.MongoLockService;
import com.linuxbox.util.lockservice.mongodb.MongoLockService.LockRequestFailure;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoLockingServiceTest {
	static DB database;
	static MongoLockService service;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Mongo mongo = new Mongo();
		UUID dbUUID = UUID.randomUUID();
		database = mongo.getDB("test:" + dbUUID.toString());
		DBCollection lockCollection = database
				.getCollection("testMongoLockingService");
		service = new MongoLockService(lockCollection);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		database.dropDatabase();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFileControlBasic() throws Exception {
		String id = UUID.randomUUID().toString();
		assertTrue(service.lock(id, "one"));
		service.releaseLock(id);
	}

	@Test
	public void testFileControlDoubleControl() throws Exception {
		String id = UUID.randomUUID().toString();
		assertTrue(service.lock(id, "one"));
		assertFalse(service.lock(id, "two"));
		service.releaseLock(id);
	}

	@Test(expected = LockReleaseException.class)
	public void testFileControlNoControl() throws Exception {
		String id = UUID.randomUUID().toString();
		service.releaseLock(id);
	}

	@Test
	public void testLockWithFailureData() throws Exception {
		final String note1 = "abracadabra";
		final String note2 = "hocus pocus";

		Date beforeStamp = new Date();

		String id = UUID.randomUUID().toString();
		Assert.assertNull(service.lockWithFailureData(id, note1));

		Date afterStamp = new Date();

		LockRequestFailure failureRecord = service.lockWithFailureData(id,
				note2);

		Assert.assertEquals(note1, failureRecord.holderNote);
		Assert.assertTrue(beforeStamp.getTime() <= failureRecord.holderTimestamp
				.getTime());
		Assert.assertTrue(afterStamp.getTime() >= failureRecord.holderTimestamp
				.getTime());

		service.releaseLock(id);
	}
}
