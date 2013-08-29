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
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.linuxbox.util.lockservice.LockReleaseException;
import com.linuxbox.util.lockservice.LockService.LockRequestFailure;
import com.linuxbox.util.lockservice.mongodb.JavaLockService;

public class JavaLockingServiceTest {

	private static JavaLockService service;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		service = new JavaLockService();
		service.startup();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		service.shutdown();
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
		service.lock(id, "two");
		service.releaseLock(id);
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

		Assert.assertEquals(note1, failureRecord.holderNote.toString());
		Assert.assertTrue(beforeStamp.getTime() <= failureRecord.holderTimestamp
				.getTime());
		Assert.assertTrue(afterStamp.getTime() >= failureRecord.holderTimestamp
				.getTime());

		service.releaseLock(id);
	}
}
