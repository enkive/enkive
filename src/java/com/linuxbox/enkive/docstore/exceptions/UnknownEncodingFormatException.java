package com.linuxbox.enkive.docstore.exceptions;

public class UnknownEncodingFormatException extends DocStoreException {
	private static final long serialVersionUID = -7337622835342485854L;

	public UnknownEncodingFormatException() {
		super("unspecified encoding");
	}

	public UnknownEncodingFormatException(String encodingFormat) {
		super("encoding: " + encodingFormat);
	}
}
