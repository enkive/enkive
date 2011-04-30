package com.linuxbox.enkive.docstore;

import java.io.InputStream;

import com.linuxbox.enkive.docstore.exception.DocStoreException;

public interface Document {
	String getIdentifier();

	/**
	 * If known will return the size of the document. If not known will return a
	 * negative value. This value should not be trusted to be exact; it's useful
	 * for estimation purposes only.
	 * 
	 * @return
	 */
	long getSize();

	String getMimeType();

	String getExtension();

	byte[] getContentBytes() throws DocStoreException;

	/**
	 * Provides an input stream that will produce the content byte by byte.
	 * 
	 * @return content of document as an input stream
	 * @throws DocStoreException
	 */
	InputStream getContentStream() throws DocStoreException;
}
