package com.linuxbox.enkive.docstore;

import java.io.Reader;

import com.linuxbox.enkive.docstore.exceptions.DocStoreException;

public interface EncodedDocument extends Document {
	String getEncodedContentString() throws DocStoreException;	
	char[] getEncodedContentChars() throws DocStoreException;
	Reader getEncodedContentReader() throws DocStoreException;
}
