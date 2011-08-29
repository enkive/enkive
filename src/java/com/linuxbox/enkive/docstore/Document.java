package com.linuxbox.enkive.docstore;

import java.io.InputStream;

import com.linuxbox.enkive.docstore.exception.DocStoreException;

public interface Document {
	/**
	 * If known will return the size of the document. If not known will return a
	 * negative value. This value should not be trusted to be exact; it's useful
	 * for estimation purposes only.
	 * 
	 * @return
	 */
	long getEncodedSize();

	String getMimeType();
	
	String getFilename();

	String getFileExtension();

	String getBinaryEncoding();

	/**
	 * Provides an input stream that will produce the encoded content byte by
	 * byte.
	 * 
	 * @return encoded content of document as an input stream
	 * @throws DocStoreException
	 */
	InputStream getEncodedContentStream() throws DocStoreException;

	/**
	 * Provides an input stream that will produce the decoded content byte by
	 * byte.
	 * 
	 * @return decoded content of document as an input stream
	 * @throws DocStoreException
	 */
	InputStream getDecodedContentStream() throws DocStoreException;
}
