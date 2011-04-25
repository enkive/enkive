package com.linuxbox.enkive.docstore.exception;

public class DocumentTooLargeException extends DocStoreException {
	private static final long serialVersionUID = -2447730635027956586L;

	public DocumentTooLargeException(long size, long max) {
		super("Document size is " + size + ", yet maximum size is " + max);
	}
}
