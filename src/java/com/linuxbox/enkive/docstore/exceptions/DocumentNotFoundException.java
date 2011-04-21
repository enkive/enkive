package com.linuxbox.enkive.docstore.exceptions;

public class DocumentNotFoundException extends DocStoreException {
	private static final long serialVersionUID = -6412368633620713809L;

	public DocumentNotFoundException(String key) {
		super("key: " + key);
	}
}
