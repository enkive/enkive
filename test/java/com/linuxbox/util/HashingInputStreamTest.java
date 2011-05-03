package com.linuxbox.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HashingInputStreamTest {
	static String HASH_ALGORITHM = "SHA-1";
	static byte[] message;
	static String digestReference;

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
		byte[] digestBytes = md.digest();
		digestReference = new String((new Hex()).encode(digestBytes));
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
		String digest = input.getDigest();
		assertEquals("testing equality of digests", digestReference, digest);
	}

	@Test
	public void testReadByteArray() throws IOException {
		byte[] buffer = new byte[4 * 1024];
		while (input.read(buffer) > 0) {
			// do nothing
		}
		String digest = input.getDigest();
		assertEquals("testing equality of digests", digestReference, digest);
	}

	@Test
	public void testReadByteArrayIntInt() throws IOException {
		final int start = 17;
		final int len = 3433; // prime number
		byte[] buffer = new byte[4 * 1024];
		while (input.read(buffer, start, len) > 0) {
			// do nothing
		}
		String digest = input.getDigest();
		assertEquals("testing equality of digests", digestReference, digest);
	}

	@Test
	public void testSkip() throws IOException {
		byte[] buffer = new byte[512];
		input.read(buffer);
		input.skip(2 * buffer.length);
		while (input.read(buffer) > 0) {
			// do nothing
		}
		String digest = input.getDigest();
		assertEquals("testing equality of digests", digestReference, digest);
	}

	@Test
	public void testMarkSupported() {
		assertFalse(input.markSupported());
	}
}
