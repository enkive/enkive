package com.linuxbox.enkive.docstore;

import com.linuxbox.enkive.docstore.exceptions.DocStoreException;


public class SimpleDocument extends InMemorySHA1Document {
	private String dataString;
	
	public SimpleDocument(String dataString, String mimeType, String suffix)
			throws DocStoreException {
		super(mimeType, suffix, dataString.getBytes());
		this.dataString = dataString;
	}
	
	public String getString() {
		return dataString;
	}
}
