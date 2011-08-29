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
import com.mongodb.Mongo;

public class MongoLockingServiceTest {
	private static final String databaseName = "test-mongo-locking-service";
	private static final String collectionName = "lockingService";

	private static Mongo mongo;
	private static MongoLockService service;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mongo = new Mongo();

		service = new MongoLockService(mongo, databaseName, collectionName);
		service.startup();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		service.shutdown();

		DB database = mongo.getDB(databaseName);
		database.dropDatabase();
		mongo.close();
	}

	@Before
	public void setUp() throws Exception {
		// empty
	}

	@After
	public void tearDown() throws Exception {
		// empty
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
