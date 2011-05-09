package com.linuxbox.enkive.docstore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.linuxbox.enkive.docstore.mongogrid.Constants;

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
		return new ByteArrayInputStream(
				dataString.getBytes(Constants.PREFERRED_CHARSET));
	}

	@Override
	public InputStream getDecodedContentStream() {
		return new ByteArrayInputStream(
				dataString.getBytes(Constants.PREFERRED_CHARSET));
	}

	public String getString() {
		return dataString;
	}
}
