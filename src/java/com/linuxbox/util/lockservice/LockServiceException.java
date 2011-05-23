package com.linuxbox.util.lockservice;

public abstract class LockServiceException extends Exception {
	private static final long serialVersionUID = 8706352984347388472L;

	public LockServiceException() {
		super();
	}

	public LockServiceException(String message) {
		super(message);
	}

	public LockServiceException(Throwable thrown) {
		super(thrown);
	}

	public LockServiceException(String message, Throwable thrown) {
		super(message, thrown);
	}
}
