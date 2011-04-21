package com.linuxbox.enkive.docstore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import com.linuxbox.enkive.exception.EnkiveRuntimeException;

public class SimpleDocument implements Document {
	private static final String HASH_ALGORITHM = "SHA-1";

	private String dataString;
	private String mimeType;
	private String suffix;
	private byte[] dataBytes;

	protected byte[] hashBytes;
	protected String hashString;
	
	public SimpleDocument(String dataString, String mimeType, String suffix) {
		this.dataString = dataString;
		this.mimeType = mimeType;
		this.suffix = suffix;
	}

	@Override
	public byte[] getContentBytes() {
		if (dataBytes == null) {
			dataBytes = dataString.getBytes();
		}
		return dataBytes;
	}

	@Override
	public InputStream getContentStream() {
		return new ByteArrayInputStream(getContentBytes());
	}

	@Override
	public String getIdentifier() {
		return getSha1String();
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public String getSuffix() {
		return suffix;
	}
	
	private String getSha1String() {
		// only calculate hash when requested
		if (hashString == null) {
			calculateHash();
		}
		return hashString;
	}
	
	private void calculateHash() {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new EnkiveRuntimeException(e.getMessage());
		}

		md.update(getContentBytes());

		hashBytes = md.digest();
		hashString = new String((new Hex()).encode(hashBytes));
	}
}
