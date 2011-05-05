package com.linuxbox.enkive.docstore;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.util.HashingInputStream;

public abstract class AbstractDocStoreService implements DocStoreService {
	private static final String HASH_ALGORITHM = "SHA-1";
	private final static int BUFFER_SIZE = 16 * 1024;

	/**
	 * Stores the document in the back-end if the name is known and the data is
	 * in a byte array
	 * 
	 * @param the
	 *            Document, so mime type, file extension, and binary encoding
	 *            can be determined
	 * @param identifier
	 *            the known name of the identifier
	 * @param data
	 *            the actual data for the file
	 * @param length
	 *            the length of the used portion of data; everything after is
	 *            junk
	 * @return true if the file was already stored, false if it was just created
	 */
	protected abstract boolean storeKnownName(Document document,
			String identifier, byte[] data, int length)
			throws DocStoreException;

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
	protected abstract StoreRequestResult storeAndDetermineName(
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

		StringBuffer headerBuffer = new StringBuffer();
		headerBuffer.append(cleanStringComponent(document.getMimeType()));
		headerBuffer.append(";");
		headerBuffer.append(cleanStringComponent(document.getFileExtension()));
		headerBuffer.append(";");
		headerBuffer.append(cleanStringComponent(document.getBinaryEncoding()));
		headerBuffer.append(";");
		messageDigest.update(headerBuffer.toString().getBytes());

		try {
			// create a fix-sized buffer to see if the data will fit within it

			BufferedInputStream inputStream = new BufferedInputStream(
					document.getEncodedContentStream(), BUFFER_SIZE);
			inputStream.mark(BUFFER_SIZE);

			// try to read all of the data into a fix-sized buffer

			byte[] buffer = new byte[BUFFER_SIZE];
			int offset = 0;
			int result;
			do {
				result = inputStream.read(buffer, offset, BUFFER_SIZE - offset);
				if (result > 0) {
					offset += result;
				}
			} while (result >= 0 && offset < BUFFER_SIZE);

			if (result < 0) {
				// was able to read whole thing in and offset indicates length
				messageDigest.update(buffer, 0, offset);
				final byte[] hashBytes = messageDigest.digest();
				String identifier = new String((new Hex()).encode(hashBytes));
				boolean alreadyStored = storeKnownName(document, identifier,
						buffer, offset);
				return new StoreRequestResultImpl(identifier, alreadyStored);
			} else {
				// could not read whole thing into fix-sized buffer, so store
				// the document, determine its name after-the fact, and rename
				// it
				inputStream.reset();
				HashingInputStream hashedInputStream = new HashingInputStream(
						messageDigest, inputStream);
				StoreRequestResult storeResult = storeAndDetermineName(
						document, hashedInputStream);
				return storeResult;
			}
		} catch (IOException e) {
			throw new DocStoreException(e);
		}
	}

	private String cleanStringComponent(String s) {
		if (s == null) {
			return "";
		} else {
			return s.trim();
		}
	}
}
