package com.linuxbox.enkive.docstore;

import static org.junit.Assert.*;

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
	public void testGetShardIndexFromHash() {
		int i;

		byte[] b1 = { 0x60, 0x60 };
		i = AbstractDocStoreService.getShardIndexFromHash(b1);
		assertEquals(0x6060, i);

		byte[] b2 = { -0x60, 0x60 };
		i = AbstractDocStoreService.getShardIndexFromHash(b2);
		assertEquals(0xa060, i);

		byte[] b3 = { 0x60, -0x60 };
		i = AbstractDocStoreService.getShardIndexFromHash(b3);
		assertEquals(0x60a0, i);

		byte[] b4 = { -0x60, -0x60 };
		i = AbstractDocStoreService.getShardIndexFromHash(b4);
		assertEquals(0xa0a0, i);
	}

	@Test
	public void testGetFileNameFromHash() {
		String s;

		byte[] b1 = { 0x00, 0x01, 0x02, 0x03, -0x60, -0x01 };
		s = AbstractDocStoreService.getFileNameFromHash(b1);
		assertEquals("00010203a0ff", s);

		byte[] b2 = new byte[16];
		s = AbstractDocStoreService.getFileNameFromHash(b2);
		assertEquals("00000000000000000000000000000000", s);
	}

}
