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
 *******************************************************************************/
package com.linuxbox.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

/**
 * This is a stream that calculates a hash/message digest on the data that
 * passes through it. It can use either an existing message digest (say if it's
 * primed with other data) or create one if provided with a String describing
 * the algorithm to use.
 * 
 * Once getDigest() is called, further data will not affect the hash/message
 * digest. So the use case is to call that once EOF is reached. Perhaps a better
 * version could clone the message digest and return the hash/message digest on
 * it. But message digests are not guaranteed to support the clone operation.
 * 
 * @author ivancich
 * 
 */
public class HashingInputStream extends InputStream {
	public final static String DEFAULT_ALGORITHM = "SHA-1";
	private final static int DEFAULT_SKIP_BUFFER_SIZE = 4 * 1024;

	private MessageDigest digest;
	private InputStream actualInputStream;
	private byte[] digestBytes;
	private int skipBufferSize;

	public HashingInputStream(MessageDigest digest,
			InputStream actualInputStream) {
		this.digest = digest;
		this.actualInputStream = actualInputStream;
		this.skipBufferSize = DEFAULT_SKIP_BUFFER_SIZE;
	}

	public HashingInputStream(String hashAlgorithm,
			InputStream actualInputStream) throws NoSuchAlgorithmException {
		this(MessageDigest.getInstance(hashAlgorithm), actualInputStream);
	}

	public byte[] getDigest() {
		if (digestBytes == null) {
			digestBytes = digest.digest();
		}
		return digestBytes;
	}

	public String getDigestString() {
		return new String((new Hex()).encode(getDigest()));
	}

	@Override
	public int available() throws IOException {
		return actualInputStream.available();
	}

	@Override
	public void close() throws IOException {
		actualInputStream.close();
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public int read() throws IOException {
		int result = actualInputStream.read();
		if (result >= 0) {
			digest.update((byte) result);
		}
		return result;
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		int bytesRead = actualInputStream.read(buffer, offset, length);
		if (bytesRead > 0) {
			digest.update(buffer, offset, bytesRead);
		}
		return bytesRead;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public long skip(long toSkip) throws IOException {
		byte[] buffer = new byte[skipBufferSize];

		long totalRead = 0;
		int result;
		do {
			int toRead = (toSkip - totalRead > skipBufferSize) ? skipBufferSize
					: (int) (toSkip - totalRead);
			result = read(buffer, 0, toRead); // computes digest on its own
			if (result > 0) {
				totalRead += result;
			}
		} while (totalRead < toSkip && result >= 0);

		return totalRead;
	}

	public int getSkipBufferSize() {
		return skipBufferSize;
	}

	public void setSkipBufferSize(int skipBufferSize) {
		this.skipBufferSize = skipBufferSize;
	}
}
