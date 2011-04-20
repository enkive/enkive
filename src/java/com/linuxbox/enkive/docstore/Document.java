package com.linuxbox.enkive.docstore;

import java.io.InputStream;

public interface Document {
	String getMimeType();
	String getSuffix();
	
	byte[] getContentBytes();
	
	/**
	 * Provides an input stream that will produce the content byte by byte.
	 * @return content of document as an input stream
	 */
	InputStream getContentStream();
}
