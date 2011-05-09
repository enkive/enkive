package com.linuxbox.util.mongodb;

public abstract class MongoLockingServiceException extends Exception {
	private static final long serialVersionUID = 8706352984347388472L;

	public MongoLockingServiceException() {
		super();
	}

	public MongoLockingServiceException(String message) {
		super(message);
	}

	public MongoLockingServiceException(Throwable thrown) {
		super(thrown);
	}

	public MongoLockingServiceException(String message, Throwable thrown) {
		super(message, thrown);
	}
}
