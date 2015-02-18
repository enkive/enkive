/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.docstore;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AbstractDocStoreServiceTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
	public void testGetShardIndexFromIdentifer() {
		int i;
		int j;

		byte[] b1 = { 0x6a, 0x2f, 0x7e };
		String s1 = AbstractDocStoreService.getIdentifierFromHash(b1);
		i = AbstractDocStoreService.getShardIndexFromHash(b1);
		j = AbstractDocStoreService.getShardIndexFromIdentifier(s1);
		assertEquals(i, j);

		byte[] b2 = { -0x3b, 0x19, 0x7f };
		String s2 = AbstractDocStoreService.getIdentifierFromHash(b2);
		i = AbstractDocStoreService.getShardIndexFromHash(b2);
		j = AbstractDocStoreService.getShardIndexFromIdentifier(s2);
		assertEquals(i, j);

		byte[] b3 = { 0x60, -0x4d, -0x80 };
		String s3 = AbstractDocStoreService.getIdentifierFromHash(b3);
		i = AbstractDocStoreService.getShardIndexFromHash(b3);
		j = AbstractDocStoreService.getShardIndexFromIdentifier(s3);
		assertEquals(i, j);

		byte[] b4 = { -0x21, -0x22, -0x7f };
		String s4 = AbstractDocStoreService.getIdentifierFromHash(b4);
		i = AbstractDocStoreService.getShardIndexFromHash(b4);
		j = AbstractDocStoreService.getShardIndexFromIdentifier(s4);
		assertEquals(i, j);
	}

	@Test
	public void testGetShardIndexFromHash() {
		int i;

		byte[] b1 = { 0x60, 0x60, 0x7e };
		i = AbstractDocStoreService.getShardIndexFromHash(b1);
		assertEquals(0x6060, i);

		byte[] b2 = { -0x60, 0x60, 0x7f };
		i = AbstractDocStoreService.getShardIndexFromHash(b2);
		assertEquals(0xa060, i);

		byte[] b3 = { 0x60, -0x60, -0x80 };
		i = AbstractDocStoreService.getShardIndexFromHash(b3);
		assertEquals(0x60a0, i);

		byte[] b4 = { -0x60, -0x60, -0x7f };
		i = AbstractDocStoreService.getShardIndexFromHash(b4);
		assertEquals(0xa0a0, i);
	}

	@Test
	public void testGetFileNameFromHash() {
		String s;

		byte[] b1 = { 0x00, 0x01, 0x02, 0x03, -0x60, -0x01 };
		s = AbstractDocStoreService.getIdentifierFromHash(b1);
		assertEquals("00010203a0ff", s);

		byte[] b2 = new byte[16];
		s = AbstractDocStoreService.getIdentifierFromHash(b2);
		assertEquals("00000000000000000000000000000000", s);
	}

}
