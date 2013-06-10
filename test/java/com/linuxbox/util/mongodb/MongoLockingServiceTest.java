/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
 * 
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
package com.linuxbox.util.mongodb;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.util.lockservice.LockAcquisitionException;
import com.linuxbox.util.lockservice.LockReleaseException;
import com.linuxbox.util.lockservice.mongodb.MongoLockService;
import com.linuxbox.util.lockservice.mongodb.MongoLockService.LockRequestFailure;
import com.linuxbox.util.mongodb.MongoIndexable.IndexDescription;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoLockingServiceTest {

	private static Mongo mongo;
	private static MongoLockService service;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mongo = new Mongo();

		service = new MongoLockService(mongo,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_LOCK_COLLECTION);
		List<IndexDescription> indexes = service.getPreferredIndexes();
		for(IndexDescription index : indexes){
			final DBObject options = BasicDBObjectBuilder.start()
					.add("name", index.getName())
					.add("unique", index.isUnique())
					.add("background", false).get();
			service.ensureIndex(index.getDescription(), options);
		}
		service.startup();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		service.shutdown();
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
		service.lock(id, "one");
		service.releaseLock(id);
	}

	@Test
	public void testFileControlDoubleControl() throws Exception {
		String id = UUID.randomUUID().toString();
		service.lock(id, "one");
		exception.expect(LockAcquisitionException.class);
		service.lock(id, "two");
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
