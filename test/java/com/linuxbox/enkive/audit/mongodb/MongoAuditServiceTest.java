/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
package com.linuxbox.enkive.audit.mongodb;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.audit.AuditEntry;
import com.linuxbox.enkive.audit.AuditService;
import com.mongodb.Mongo;

public class MongoAuditServiceTest {

	private static int counter = 0;

	private Mongo mongo;
	private MongoAuditService service;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// empty
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// empty
	}

	@Before
	public void setUp() throws Exception {
		mongo = new Mongo();

		service = new MongoAuditService(mongo,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_AUDIT_COLLECTION + counter++);
		service.startup();
	}

	@After
	public void tearDown() throws Exception {
		service.shutdown();
		mongo.close();
	}

	@Test
	public void testCount() throws Exception {
		long count1 = service.getAuditEntryCount();
		assertEquals(0l, count1);

		addData(service, 0);

		long count2 = service.getAuditEntryCount();
		assertEquals(5L, count2);
	}

	@Test
	public void testRetreiveById() throws Exception {
		addData(service, 0);

		List<AuditEntry> allResults = service.search(null, null, null, null);

		assertEquals(5, allResults.size());

		for (AuditEntry entry : allResults) {
			final String identifier = entry.getIdentifier();
			final AuditEntry result = service.getEvent(identifier);
			assertEquals(entry.getEventCode(), result.getEventCode());
			assertEquals(entry.getTimestamp(), result.getTimestamp());
			assertEquals(entry.getUserName(), result.getUserName());
			assertEquals(entry.getDescription(), result.getDescription());
		}
	}

	@Test
	public void testRetrieveByUser() throws Exception {
		addData(service, 501);

		List<AuditEntry> result = service.search(null, "honey", null, null);

		assertEquals(3, result.size());

		for (AuditEntry entry : result) {
			assertEquals("honey", entry.getUserName());
		}

		AuditEntry firstResult = result.get(0);
		AuditEntry lastResult = result.get(2);

		assertEquals(17, firstResult.getEventCode());
		assertEquals(1, lastResult.getEventCode());

		assertEquals("something super very nefarious",
				firstResult.getDescription());
		assertEquals("something nefarious", lastResult.getDescription());

		Assert.assertTrue(
				"first result date must be after last result date",
				lastResult.getTimestamp().compareTo(firstResult.getTimestamp()) < 0);
	}

	@Test
	public void testRetrieveByCode() throws Exception {
		addData(service, 501);

		List<AuditEntry> result = service.search(17, null, null, null);

		assertEquals(3, result.size());

		for (AuditEntry entry : result) {
			assertEquals(17, entry.getEventCode());
		}

		AuditEntry firstResult = result.get(0);
		AuditEntry lastResult = result.get(2);

		assertEquals("thomas", firstResult.getUserName());
		assertEquals("honey", lastResult.getUserName());

		assertEquals("takes over world", firstResult.getDescription());
		assertEquals("something very nefarious", lastResult.getDescription());

		Assert.assertTrue(
				"first result date must be after last result date",
				lastResult.getTimestamp().compareTo(firstResult.getTimestamp()) < 0);
	}

	@Test
	public void testRetrieveByCodeAndUser() throws Exception {
		addData(service, 501);

		List<AuditEntry> result = service.search(17, "honey", null, null);

		assertEquals(2, result.size());

		for (AuditEntry entry : result) {
			assertEquals(17, entry.getEventCode());
			assertEquals("honey", entry.getUserName());
		}

		AuditEntry firstResult = result.get(0);
		AuditEntry lastResult = result.get(1);

		assertEquals("something super very nefarious",
				firstResult.getDescription());
		assertEquals("something very nefarious", lastResult.getDescription());

		Assert.assertTrue(
				"first result date must be after last result date",
				lastResult.getTimestamp().compareTo(firstResult.getTimestamp()) < 0);
	}

	@Test
	public void testRetrieveByDates() throws Exception {
		addData(service, 1001);

		List<AuditEntry> allResults = service.search(null, null, null, null);

		assertEquals(5, allResults.size());

		List<Date> dates = new ArrayList<Date>(5);
		for (AuditEntry entry : allResults) {
			dates.add(entry.getTimestamp());
		}

		List<AuditEntry> latterResults = service.search(null, null,
				dates.get(2), null);
		assertEquals(3, latterResults.size());

		assertEquals("takes over world", latterResults.get(0).getDescription());
		assertEquals("something dramatic", latterResults.get(2)
				.getDescription());

		List<AuditEntry> formerResults = service.search(null, null, null,
				dates.get(2));
		assertEquals(2, formerResults.size());

		assertEquals("something very nefarious", formerResults.get(0)
				.getDescription());
		assertEquals("something nefarious", formerResults.get(1)
				.getDescription());

		List<AuditEntry> middleResults = service.search(null, null,
				dates.get(3), dates.get(1));
		assertEquals(2, middleResults.size());

		assertEquals("something dramatic", middleResults.get(0)
				.getDescription());
		assertEquals("something very nefarious", middleResults.get(1)
				.getDescription());

		List<AuditEntry> nullResults1 = service.search(null, null,
				dates.get(1), dates.get(3));
		assertEquals("we get no results when the start date is > end date", 0,
				nullResults1.size());

		List<AuditEntry> nullResults2 = service.search(null, null,
				dates.get(2), dates.get(2));
		assertEquals("we get no results when the start date is == end date", 0,
				nullResults2.size());
	}

	/**
	 * If we say the page size is 2, and if there are 5 entries, we should get
	 * them in groups of 2, 2, and then 1. Plus it returns them in
	 * reverse-chronological order.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRetrievePaged() throws Exception {
		addData(service, 0);
		for (int i = 0; i <= 2; i++) {
			List<AuditEntry> result = service.getMostRecentByPage(2, i);
			switch (i) {
			case 0:
				assertEquals(2, result.size());
				assertEquals("takes over world", result.get(0).getDescription());
				break;
			case 1:
				assertEquals(2, result.size());
				assertEquals("something dramatic", result.get(0)
						.getDescription());
				break;
			case 2:
				assertEquals(1, result.size());
				assertEquals("something nefarious", result.get(0)
						.getDescription());
				break;
			default:
				Assert.fail("should never get here");
			}
		}
	}

	private static void addData(AuditService service, long delay)
			throws Exception {
		service.addEvent(1, "honey", "something nefarious");
		Thread.sleep(delay);
		service.addEvent(17, "honey", "something very nefarious");
		Thread.sleep(delay);
		service.addEvent(12, "adam", "something dramatic");
		Thread.sleep(delay);
		service.addEvent(17, "honey", "something super very nefarious");
		Thread.sleep(delay);
		service.addEvent(17, "thomas", "takes over world");
	}
}
