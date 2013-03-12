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
/**
 * TODO This test needs to be updated to use the enkivefilterbean and read in a test filter xml file.
 */
package com.linuxbox.enkive.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.exception.BadMessageException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageImpl;

public class EnkiveFilterTest {

	protected static Message message;
	protected static EnkiveFiltersBean enkiveFilterBean;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		InputStream filestream = new FileInputStream(new File(
				TestingConstants.TEST_MESSAGE_DIRECTORY
						+ "/filterTest/basic-plain.msg"));
		message = new MessageImpl(filestream);
		Calendar date = Calendar.getInstance();
		date.set(1997, 2, 11, 14, 0, 0);

		message.setDate(date.getTime());
		filestream.close();
		enkiveFilterBean = new EnkiveFiltersBean();
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
	public void testIntegerLessThanAllow() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-int-flag",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.FLOAT, "3",
				EnkiveFilterConstants.FilterComparator.IS_LESS_THAN);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testIntegerLessThanDeny() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-int-flag",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.FLOAT, "3",
				EnkiveFilterConstants.FilterComparator.IS_LESS_THAN);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testIntegerGreaterThanAllow() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-int-flag",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.FLOAT, "1",
				EnkiveFilterConstants.FilterComparator.IS_GREATER_THAN);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testIntegerGreaterThanDeny() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-int-flag",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.FLOAT, "1",
				EnkiveFilterConstants.FilterComparator.IS_GREATER_THAN);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testIntegerMatchesAllow() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-int-flag",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.FLOAT, "2",
				EnkiveFilterConstants.FilterComparator.MATCHES);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));

	}

	@Test
	public void testIntegerMatchesDeny() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-int-flag",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.FLOAT, "2",
				EnkiveFilterConstants.FilterComparator.MATCHES);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testIntegerDoesNotMatchAllow() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-int-flag",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.FLOAT, "1",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_MATCH);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));

	}

	@Test
	public void testIntegerDoesNotMatchDeny() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-int-flag",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.FLOAT, "1",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_MATCH);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));

	}

	@Test
	public void testFloatLessThanAllow() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-float-flag",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.FLOAT, "3.0",
				EnkiveFilterConstants.FilterComparator.IS_LESS_THAN);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));

	}

	@Test
	public void testFloatLessThanDeny() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-float-flag",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.FLOAT, "3.0",
				EnkiveFilterConstants.FilterComparator.IS_LESS_THAN);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testFloatGreaterThanAllow() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-float-flag",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.FLOAT, "1.0",
				EnkiveFilterConstants.FilterComparator.IS_GREATER_THAN);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));

	}

	@Test
	public void testFloatGreaterThanDeny() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-float-flag",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.FLOAT, "1.0",
				EnkiveFilterConstants.FilterComparator.IS_GREATER_THAN);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testFloatMatchesAllow() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-float-flag",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.FLOAT, "2.0",
				EnkiveFilterConstants.FilterComparator.MATCHES);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testFloatMatchesDeny() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-float-flag",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.FLOAT, "2.0",
				EnkiveFilterConstants.FilterComparator.MATCHES);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testFloatDoesNotMatchAllow() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-float-flag",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.FLOAT, "2.1",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_MATCH);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testFloatDoesNotMatchDeny() {
		EnkiveFilter filter = new EnkiveFilter("x-filter-test-float-flag",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.FLOAT, "2.1",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_MATCH);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testDateLessThanAllow() {
		EnkiveFilter filter = new EnkiveFilter("Date",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.DATE,
				"Wed, 11 Feb 1999 12:52:00 -0500",
				EnkiveFilterConstants.FilterComparator.IS_LESS_THAN);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testDateLessThanDeny() {
		EnkiveFilter filter = new EnkiveFilter("Date",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.DATE,
				"Wed, 11 Feb 1999 12:52:00 -0500",
				EnkiveFilterConstants.FilterComparator.IS_LESS_THAN);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testDateGreaterThanAllow() {
		EnkiveFilter filter = new EnkiveFilter("Date",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.DATE,
				"Wed, 11 Feb 1996 12:50:00 -0500",
				EnkiveFilterConstants.FilterComparator.IS_GREATER_THAN);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testDateGreaterThanDeny() {
		EnkiveFilter filter = new EnkiveFilter("Date",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.DATE,
				"Wed, 11 Feb 1996 12:00:00 -0500",
				EnkiveFilterConstants.FilterComparator.IS_GREATER_THAN);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testDateMatchesAllow() {
		EnkiveFilter filter = new EnkiveFilter("Date",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.DATE,
				"Tue, 11 Mar 1997 14:00:00 -0500",
				EnkiveFilterConstants.FilterComparator.MATCHES);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testDateMatchesDeny() {
		EnkiveFilter filter = new EnkiveFilter("Date",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.DATE,
				"Tue, 11 Mar 1997 14:00:00 -0500",
				EnkiveFilterConstants.FilterComparator.MATCHES);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testDateDoesNotMatchAllow() {
		EnkiveFilter filter = new EnkiveFilter("Date",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.DATE,
				"Wed, 11 Feb 1998 12:50:00 -0500",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_MATCH);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testDateDoesNotMatchDeny() {
		EnkiveFilter filter = new EnkiveFilter("Date",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.DATE,
				"Wed, 11 Feb 1998 12:50:00 -0500",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_MATCH);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testStringMatchesAllow() {
		EnkiveFilter filter = new EnkiveFilter("Subject",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.STRING, "Simple Subject",
				EnkiveFilterConstants.FilterComparator.MATCHES);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testStringMatchesDeny() {
		EnkiveFilter filter = new EnkiveFilter("Subject",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.STRING, "Simple Subject",
				EnkiveFilterConstants.FilterComparator.MATCHES);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testStringDoesNotMatchAllow() {
		EnkiveFilter filter = new EnkiveFilter("Subject",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.STRING,
				"This is not the subject",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_MATCH);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testStringDoesNotMatchDeny() {
		EnkiveFilter filter = new EnkiveFilter("Subject",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.STRING,
				"This is not the subject",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_MATCH);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testStringContainsAllow() {
		EnkiveFilter filter = new EnkiveFilter("Subject",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.STRING, "subject",
				EnkiveFilterConstants.FilterComparator.CONTAINS);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testStringContainsDeny() {
		EnkiveFilter filter = new EnkiveFilter("Subject",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.STRING, "subject",
				EnkiveFilterConstants.FilterComparator.CONTAINS);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testStringDoesNotContainAllow() {
		EnkiveFilter filter = new EnkiveFilter("Subject",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.STRING, "notthesubject",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_CONTAIN);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testStringDoesNotContainDeny() {
		EnkiveFilter filter = new EnkiveFilter("Subject",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.STRING, "notthesubject",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_CONTAIN);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testAddressMatchesAllow() throws BadMessageException {
		EnkiveFilter filter = new EnkiveFilter("From",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.ADDRESS, "foo@example.com",
				EnkiveFilterConstants.FilterComparator.MATCHES);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testAddressMatchesDeny() {
		EnkiveFilter filter = new EnkiveFilter("From",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.ADDRESS, "foo@example.com",
				EnkiveFilterConstants.FilterComparator.MATCHES);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testAddressDoesNotMatchAllow() {
		EnkiveFilter filter = new EnkiveFilter("To",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.ADDRESS, "foo@example.com",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_MATCH);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testAddressDoesNotMatchDeny() {
		EnkiveFilter filter = new EnkiveFilter("To",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.ADDRESS, "foo@example.com",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_MATCH);

		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testAddressContainsAllow() {
		EnkiveFilter filter = new EnkiveFilter("Cc",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.ADDRESS,
				"recipient1@example.com",
				EnkiveFilterConstants.FilterComparator.CONTAINS);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testAddressContainsDeny() {
		EnkiveFilter filter = new EnkiveFilter("Cc",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.ADDRESS,
				"recipient1@example.com",
				EnkiveFilterConstants.FilterComparator.CONTAINS);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testAddressDoesNotContainAllow() {
		EnkiveFilter filter = new EnkiveFilter("To",
				EnkiveFilterConstants.FilterAction.ALLOW,
				EnkiveFilterConstants.FilterType.ADDRESS,
				"notarecipient@example.com",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_CONTAIN);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.DENY);
		assertTrue(enkiveFilterBean.filterMessage(message));
	}

	@Test
	public void testAddressDoesNotContainDeny() {
		EnkiveFilter filter = new EnkiveFilter("To",
				EnkiveFilterConstants.FilterAction.DENY,
				EnkiveFilterConstants.FilterType.ADDRESS,
				"notarecipient@example.com",
				EnkiveFilterConstants.FilterComparator.DOES_NOT_CONTAIN);
		HashSet<EnkiveFilter> filterSet = new HashSet<EnkiveFilter>();
		filterSet.add(filter);
		enkiveFilterBean.setFilterSet(filterSet);
		enkiveFilterBean
				.setDefaultAction(EnkiveFilterConstants.FilterAction.ALLOW);
		assertFalse(enkiveFilterBean.filterMessage(message));
	}

}
