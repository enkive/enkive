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
package com.linuxbox.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HashingInputStreamTest {
	static String HASH_ALGORITHM = HashingInputStream.DEFAULT_ALGORITHM;
	static byte[] message;
	static byte[] digestReference;

	HashingInputStream input;

	@BeforeClass
	public static void setUpClass() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int b = 1; b <= 255; b++) {
			for (int count = 1; count <= b; count++) {
				out.write((byte) b);
			}
		}
		message = out.toByteArray();
		out.close();

		MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
		md.update(message);
		digestReference = md.digest();
	}

	@Before
	public void setUp() throws Exception {
		input = new HashingInputStream(HASH_ALGORITHM,
				new ByteArrayInputStream(message));
	}

	@After
	public void tearDown() throws Exception {
		input.close();
	}

	@Test
	public void testRead() throws IOException {
		while (input.read() >= 0) {
			// do nothing
		}
		assertArrayEquals("testing equality of digests", digestReference,
				input.getDigest());
	}

	@Test
	public void testReadByteArray() throws IOException {
		byte[] buffer = new byte[4 * 1024];
		while (input.read(buffer) > 0) {
			// do nothing
		}
		assertArrayEquals("testing equality of digests", digestReference,
				input.getDigest());
	}

	@Test
	public void testReadByteArrayIntInt() throws IOException {
		final int start = 17;
		final int len = 3433; // prime number
		byte[] buffer = new byte[4 * 1024];
		while (input.read(buffer, start, len) > 0) {
			// do nothing
		}
		assertArrayEquals("testing equality of digests", digestReference,
				input.getDigest());
	}

	@Test
	public void testSkip() throws IOException {
		byte[] buffer = new byte[512];
		input.read(buffer);
		input.skip(2 * buffer.length);
		while (input.read(buffer) > 0) {
			// do nothing
		}
		assertArrayEquals("testing equality of digests", digestReference,
				input.getDigest());
	}

	@Test
	public void testMarkSupported() {
		assertFalse(input.markSupported());
	}
}
