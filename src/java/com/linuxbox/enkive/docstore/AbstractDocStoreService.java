package com.linuxbox.enkive.docstore;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.util.HashingInputStream;

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
	private static final int DEFAULT_IN_MEMORY_LIMIT = 16 * 1024; // 16 KB
	public static final String HASH_ALGORITHM = "SHA-1";
	public static final int INDEX_SHARD_KEY_COUNT = 256;

	/**
	 * The limit as to whether a document will be processed in memory.
	 */
	private int inMemoryLimit;

	/**
	 * The in-memory buffer to use.
	 */
	// private byte[] inMemoryBuffer;

	public AbstractDocStoreService() {
		this(DEFAULT_IN_MEMORY_LIMIT);
	}

	public AbstractDocStoreService(int inMemoryLimit) {
		setInMemoryLimit(inMemoryLimit);
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
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new DocStoreException(e);
		}

		// begin the hash calculation using the mime type, file extension, and
		// binary encoding, so if the same data comes in but is claimed to be a
		// different in any of those aspects, it will be stored separately; we
		// don't expect this to happen often if at all, but doing so makes
		// everything else easier
		messageDigest.update(getFileTypeEncodingDigestPrime(document));

		byte[] inMemoryBuffer = new byte[inMemoryLimit];
		try {
			// create a fix-sized buffer to see if the data will fit within it

			BufferedInputStream inputStream = new BufferedInputStream(
					document.getEncodedContentStream(), inMemoryLimit);
			inputStream.mark(inMemoryLimit);

			// try to read all of the data into a fix-sized buffer

			int offset = 0;
			int result;
			do {
				result = inputStream.read(inMemoryBuffer, offset, inMemoryLimit
						- offset);
				if (result > 0) {
					offset += result;
				}
			} while (result >= 0 && offset < inMemoryLimit);

			if (result < 0) {
				// was able to read whole thing in and offset indicates length
				messageDigest.update(inMemoryBuffer, 0, offset);
				final byte[] hashBytes = messageDigest.digest();
				StoreRequestResult storeResult = storeKnownHash(document,
						hashBytes, inMemoryBuffer, offset);
				return storeResult;
			} else {
				// could not read whole thing into fix-sized buffer, so store
				// the document, determine its name after-the fact, and rename
				// it
				inputStream.reset();
				HashingInputStream hashedInputStream = new HashingInputStream(
						messageDigest, inputStream);
				StoreRequestResult storeResult = storeAndDetermineHash(
						document, hashedInputStream);
				return storeResult;
			}
		} catch (IOException e) {
			throw new DocStoreException(e);
		}
	}

	@Override
	public boolean removeWithRetries(String identifier, int numberOfAttempts,
			int millisecondsBetweenRetries) throws DocStoreException {
		DocStoreException lastException = null;

		for (int i = 0; i < numberOfAttempts; i++) {
			try {
				return remove(identifier);
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
	protected abstract String nextUnindexedByShardKey(int shardKeyLow,
			int shardKeyHigh);

	@Override
	public String nextUnindexed(int serverNumber, int serverCount) {
		final float perServer = (float) INDEX_SHARD_KEY_COUNT / serverCount;
		final int startRange = Math.round(serverNumber * perServer);
		final int endRange = Math.round((serverNumber + 1) * perServer);
		return nextUnindexedByShardKey(startRange, endRange);
	}
	

	/**
	 * Returns a shard key in the range of 0-255 (unsigned byte). It converts
	 * the first byte of the hash into an unsigned byte value.
	 * 
	 * @param hash
	 * @return
	 */
	protected static int getShardIndexFromHash(byte[] hash) {
		return hash[0] < 0 ? 256 + hash[0] : hash[0];
	}

	/**
	 * Returns a string representation of an array of bytes.
	 * 
	 * @param hash
	 * @return
	 */
	protected static String getFileNameFromHash(byte[] hash) {
		return new String((new Hex()).encode(hash));
	}
}
