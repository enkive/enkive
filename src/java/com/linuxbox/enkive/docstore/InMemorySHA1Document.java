package com.linuxbox.enkive.docstore;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import com.linuxbox.enkive.docstore.exceptions.DocStoreException;
import com.linuxbox.enkive.exception.EnkiveRuntimeException;

public class InMemorySHA1Document extends InMemoryDocument {
	private static final String HASH_ALGORITHM = "SHA-1";

	private byte[] hashBytes;

	public InMemorySHA1Document(String mimeType, String suffix, byte[] data)
			throws DocStoreException {
		super(null, mimeType, suffix, data);
		calculateHash();
	}

	public InMemorySHA1Document(String mimeType, String suffix,
			InputStream dataStream) throws IOException, DocStoreException {
		super(null, mimeType, suffix, dataStream);
		calculateHash();
	}

	public byte[] getSHA1Hash() {
		return hashBytes;
	}

	private void calculateHash() throws DocStoreException {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new EnkiveRuntimeException(e.getMessage());
		}

		md.update(getContentBytes());

		hashBytes = md.digest();
		setIdentifier(new String((new Hex()).encode(hashBytes)));
	}
}
