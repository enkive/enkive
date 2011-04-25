package com.linuxbox.enkive.docstore;

import java.io.InputStream;
import java.io.Reader;

import com.linuxbox.enkive.docstore.exception.DocStoreException;

public interface EncodedDocument extends Document {
	String getBinaryEncoding();
	String getEncodedContentString() throws DocStoreException;	
	char[] getEncodedContentChars() throws DocStoreException;
	Reader getEncodedContentReader() throws DocStoreException;
	InputStream getEncodedContentStream() throws DocStoreException;
}
