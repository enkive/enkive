package com.linuxbox.enkive.docsearch.exception;

public class DocSearchException extends Exception {
	private static final long serialVersionUID = 9086839326060885349L;

	public DocSearchException(String message) {
		super(message);
	}

	public DocSearchException(String message, Throwable t) {
		super(message, t);
	}

	public DocSearchException(Throwable t) {
		super(t);
	}
}
