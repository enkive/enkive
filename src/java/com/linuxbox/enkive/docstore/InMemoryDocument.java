package com.linuxbox.enkive.docstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.util.StreamConnector;

public class InMemoryDocument implements Document {
	private String identifier;
	private String mimeType;
	private String suffix;
	private byte[] data;

	public InMemoryDocument(String identifier, String mimeType, String suffix,
			byte[] data) {
		this(identifier, mimeType, suffix);
		this.data = data;
	}

	public InMemoryDocument(String identifier, String mimeType, String suffix,
			InputStream dataStream) throws IOException {
		this(identifier, mimeType, suffix);
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		StreamConnector.transferForeground(dataStream, byteStream);
		byteStream.close();
		data = byteStream.toByteArray();
	}

	private InMemoryDocument(String identifier, String mimeType, String suffix) {
		this.identifier = identifier;
		this.mimeType = mimeType;
		this.suffix = suffix;
	}

	@Override
	public byte[] getContentBytes() throws DocStoreException {
		return data;
	}

	@Override
	public InputStream getContentStream() throws DocStoreException {
		return new ByteArrayInputStream(data);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	protected void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public String getSuffix() {
		return suffix;
	}

	@Override
	public long getSize() {
		return data.length;
	}
}
