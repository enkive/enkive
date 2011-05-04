package com.linuxbox.enkive.docstore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class StringDocument extends AbstractDocument {
	private String dataString;

	public StringDocument(String dataString, String mimeType,
			String fileExtension, String binaryEncoding) {
		super(mimeType, fileExtension, binaryEncoding);
		this.dataString = dataString;
	}

	@Override
	public long getEncodedSize() {
		return dataString.length();
	}

	@Override
	public InputStream getEncodedContentStream() {
		return new ByteArrayInputStream(dataString.getBytes());
	}

	@Override
	public InputStream getDecodedContentStream() {
		return new ByteArrayInputStream(dataString.getBytes());
	}
	
	public String getString() {
		return dataString;
	}
}
