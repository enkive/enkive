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
package com.linuxbox.enkive.docstore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.util.HashingInputStream;
import com.linuxbox.util.ShardingHelper;
import com.linuxbox.util.queueservice.QueueService;
import com.linuxbox.util.queueservice.QueueServiceException;

/**
 * In trying to find the right balance between efficiency and memory usage,
 * small documents will be fully loaded into memory and the hash/digest will be
 * calculated in-place. But for larger documents, we will take another strategy
 * that likely involves storing them on the back end and calculating the
 * hash/digest as the data is being transferred. If the file is already found on
 * the back-end, it is removed.
 * 
 * @author ivancich
 * 
 */
public abstract class AbstractDocStoreService implements DocStoreService {
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.docstore");

	static final int DEFAULT_IN_MEMORY_LIMIT = 64 * 1024; // 64 KB

	public static final String HASH_ALGORITHM = "SHA-1";
	public static final int INDEX_SHARD_KEY_BYTES = 2;
	public static final int INDEX_SHARD_KEY_COUNT = 1 << (INDEX_SHARD_KEY_BYTES * 8);

	/**
	 * The limit as to whether a document will be processed in memory.
	 */
	private int inMemoryLimit;

	private QueueService indexerQueueService;

	public AbstractDocStoreService() {
		this(DEFAULT_IN_MEMORY_LIMIT);
	}

	public AbstractDocStoreService(int inMemoryLimit) {
		setInMemoryLimit(inMemoryLimit);
	}

	protected abstract void subStartup() throws DocStoreException;

	protected abstract void subShutdown() throws DocStoreException;

	public void startup() throws DocStoreException {
		if (indexerQueueService == null) {
			throw new DocStoreException("indexer queue service not set");
		}

		subStartup();
	}

	public void shutdown() throws DocStoreException {
		subShutdown();
	}

	/**
	 * Stores the document in the back-end if the name is known and the data is
	 * in a byte array
	 * 
	 * @param the
	 *            Document, so mime type, file extension, and binary encoding
	 *            can be determined
	 * @param hash
	 *            the hash of the data used to determine a unique identifier
	 *            (instrumental to the de-duplication process)
	 * @param data
	 *            the actual data for the file
	 * @param length
	 *            the length of the used portion of data; everything after is
	 *            junk
	 * @return a StoreRequestResult that contains both the name and whether the
	 *         file was already found in the back-end
	 */
	protected abstract StoreRequestResult storeKnownHash(Document document,
			byte[] hash, byte[] data, int length) throws DocStoreException;

	/**
	 * Stores the document in the back-end if the name is unknown and we just
	 * have HashingInputStream. It will likely be saved to the back-end, the
	 * name will be determined, and then a duplicate check will be made. If
	 * there is no duplicate, it will be renamed as appropriate.
	 * 
	 * @param document
	 *            the Document, so mime type, file extension, and binary
	 *            encoding can be determined
	 * @param inputStream
	 *            a HashingInputStream providing access to the data and a
	 *            hash/digest/identifier when the entire input has been read
	 * @return a StoreRequestResult that contains both the name and whether the
	 *         file was already found in the back-end
	 */
	protected abstract StoreRequestResult storeAndDetermineHash(
			Document document, HashingInputStream inputStream)
			throws DocStoreException;

	@Override
	public StoreRequestResult store(Document document) throws DocStoreException {
		StoreRequestResult storeResult = null;

		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new DocStoreException(e);
		}

		final long startTime = System.currentTimeMillis();

		// begin the hash calculation using the mime type, file extension, and
		// binary encoding, so if the same data comes in but is claimed to be a
		// different in any of those aspects, it will be stored separately; we
		// don't expect this to happen often if at all, but doing so makes
		// everything else easier
		messageDigest.update(getFileTypeEncodingDigestPrime(document));
		byte[] inMemoryBuffer = new byte[inMemoryLimit];
		try {
			InputStream originalInputStream = document
					.getEncodedContentStream();

			// keep calling read until we either fill out in-memory buffer or we
			// hit EOF
			int offset = 0;
			int result;
			do {
				result = originalInputStream.read(inMemoryBuffer, offset,
						inMemoryLimit - offset);
				if (result > 0) {
					offset += result;
				}
			} while (result >= 0 && offset < inMemoryLimit);

			if (result < 0) {
				// was able to read whole thing in and offset indicates length
				messageDigest.update(inMemoryBuffer, 0, offset);
				final byte[] hashBytes = messageDigest.digest();
				storeResult = storeKnownHash(document, hashBytes,
						inMemoryBuffer, offset);
			} else {
				// could not read whole thing into fix-sized buffer, so store
				// the document, determine its name after-the fact, and rename
				// it

				// we first need to do some input stream magic; we've already
				// read some of the data into our buffer, so convert it into an
				// input stream and then combine it and the original input
				// stream as a sequence input stream to then create a hashing
				// input stream
				ByteArrayInputStream alreadyReadStream = new ByteArrayInputStream(
						inMemoryBuffer, 0, offset);
				SequenceInputStream combinedStream = new SequenceInputStream(
						alreadyReadStream, originalInputStream);

				HashingInputStream hashingInputStream = new HashingInputStream(
						messageDigest, combinedStream);
				storeResult = storeAndDetermineHash(document,
						hashingInputStream);
			}

			if (!storeResult.getAlreadyStored()) {
				indexerQueueService.enqueue(storeResult.getIdentifier(),
						storeResult.getShardKey(),
						DocStoreConstants.QUEUE_ENTRY_INDEX_DOCUMENT);
			}

			return storeResult;
		} catch (IOException e) {
			throw new DocStoreException(e);
		} catch (QueueServiceException e) {
			throw new DocStoreException("could not add index event to queue");
		} finally {
			if (LOGGER.isTraceEnabled()) {
				final long endTime = System.currentTimeMillis();
				LOGGER.trace("TIMING: "
						+ (endTime - startTime)
						+ " ms to "
						+ (storeResult.getAlreadyStored() ? "determine already stored document "
								: "store document ")
						+ storeResult.getIdentifier());
			}
		}
	}

	@Override
	public boolean removeWithRetries(String identifier, int numberOfAttempts,
			int millisecondsBetweenRetries) throws DocStoreException {
		DocStoreException lastException = null;
		final int shardKey = getShardIndexFromIdentifier(identifier);

		for (int i = 0; i < numberOfAttempts; i++) {
			try {
				boolean result = remove(identifier);

				if (result) {
					try {
						indexerQueueService.dequeue(identifier);
						indexerQueueService.enqueue(identifier, shardKey,
								DocStoreConstants.QUEUE_ENTRY_REMOVE_DOCUMENT);
					} catch (QueueServiceException e) {
						// TODO should we throw an exception out or is logging
						// the problem enough?
						if (LOGGER.isErrorEnabled())
							LOGGER.error(
									"could not add removal of document to queue",
									e);
					}
				}

				return result;
			} catch (DocStoreException e) {
				lastException = e;
				try {
					Thread.sleep(millisecondsBetweenRetries);
				} catch (InterruptedException e2) {
					// empty
				}
			}
		}

		if (lastException != null) {
			throw lastException;
		} else if (numberOfAttempts < 1) {
			throw new DocStoreException(
					"called removeWithRetries with illegal value for number of attempts");
		} else {
			throw new DocStoreException("unknown error with removeWithRetries");
		}
	}

	public int getInMemoryLimit() {
		return inMemoryLimit;
	}

	public void setInMemoryLimit(int inMemoryLimit) {
		this.inMemoryLimit = inMemoryLimit;
	}

	public static byte[] getFileTypeEncodingDigestPrime(Document document) {
		StringBuffer primingBuffer = new StringBuffer();
		primingBuffer.append(cleanStringComponent(document.getMimeType()));
		primingBuffer.append(";");
		primingBuffer.append(cleanStringComponent(document.getFileExtension()));
		primingBuffer.append(";");
		primingBuffer
				.append(cleanStringComponent(document.getBinaryEncoding()));
		primingBuffer.append(";");
		return primingBuffer.toString().getBytes();
	}

	/**
	 * Clean up strings so differences in spaces and case don't matter (e.g.,
	 * "HTML" and "html" and " html " will all be considered to be the same.
	 * 
	 * @param s
	 * @return
	 */
	private static String cleanStringComponent(String s) {
		if (s == null) {
			return "";
		} else {
			return s.trim().toLowerCase();
		}
	}

	/**
	 * Assume only one server, index 0 of 1.
	 */
	@Override
	public String nextUnindexed() {
		return nextUnindexed(0, 1);
	}

	/**
	 * Returns a document identifier based on a range of shard keys. The shard
	 * key must be >= shardKeyLow, but < (not <=) shardKeyHigh. Or in other
	 * words the range is: shardKeyLow ... shardKeyHigh-1 .
	 * 
	 * @param shardKeyLow
	 * @param shardKeyHigh
	 * @return
	 */
	@Deprecated
	protected abstract String nextUnindexedByShardKey(int shardKeyLow,
			int shardKeyHigh);

	@Deprecated
	@Override
	public String nextUnindexed(int serverNumber, int serverCount) {
		final float perServer = (float) INDEX_SHARD_KEY_COUNT / serverCount;
		final int startRange = Math.round(serverNumber * perServer);
		final int endRange = Math.round((serverNumber + 1) * perServer);
		return nextUnindexedByShardKey(startRange, endRange);
	}

	/**
	 * Returns a shard key in the range of 0-255 (unsigned byte). It converts
	 * the first two bytes of the hash into an unsigned byte value.
	 * 
	 * @param hash
	 * @return
	 */
	protected static int getShardIndexFromHash(byte[] hash) {
		int key = hash[0] << 8 | (0xff & hash[1]);
		return key < 0 ? INDEX_SHARD_KEY_COUNT + key : key;
	}

	protected static int getHexValue(char c) {
		if (c >= '0' && c <= '9') {
			return c - '0';
		} else if (c >= 'a' && c <= 'f') {
			return c - 'a' + 10;
		} else if (c >= 'A' && c <= 'F') {
			return c - 'A' + 10;
		} else {
			return -1;
		}
	}

	public static int getShardIndexFromIdentifier(String id) {
		final char c1 = id.charAt(0);
		final char c2 = id.charAt(1);
		final char c3 = id.charAt(2);
		final char c4 = id.charAt(3);

		final int i1 = getHexValue(c1);
		final int i2 = getHexValue(c2);
		final int i3 = getHexValue(c3);
		final int i4 = getHexValue(c4);

		return (i1 << 12) + (i2 << 8) + (i3 << 4) + i4;
	}

	/**
	 * Returns a string representation of an array of bytes.
	 * 
	 * @param hash
	 * @return
	 */
	public static String getIdentifierFromHash(byte[] hash) {
		return new String((new Hex()).encode(hash));
	}

	public QueueService getIndexerQueueService() {
		return this.indexerQueueService;
	}

	public void setIndexerQueueService(QueueService indexerQueueService) {
		this.indexerQueueService = indexerQueueService;
	}

	/**
	 * Factory method so we can make use of local knowledge about how the
	 * indexing is sharded.
	 * 
	 * @param shardCount
	 * @return
	 */
	public static ShardingHelper createShardingHelper(int shardCount) {
		return new ShardingHelper(INDEX_SHARD_KEY_COUNT, shardCount);
	}
}
