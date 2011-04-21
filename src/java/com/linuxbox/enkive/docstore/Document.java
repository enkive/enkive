package com.linuxbox.enkive.docstore;

import java.io.InputStream;

import com.linuxbox.enkive.docstore.exceptions.DocStoreException;

public interface Document {
	String getIdentifier();
	
	String getMimeType();
	String getSuffix();
	
	byte[] getContentBytes() throws DocStoreException;
	
	/**
	 * Provides an input stream that will produce the content byte by byte.
	 * @return content of document as an input stream
	 * @throws DocStoreException 
	 */
	InputStream getContentStream() throws DocStoreException;
}
