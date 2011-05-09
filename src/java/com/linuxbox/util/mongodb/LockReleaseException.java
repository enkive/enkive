package com.linuxbox.util.mongodb;

public class LockReleaseException extends MongoLockingServiceException {
	private static final long serialVersionUID = 5646914906405589115L;

	public LockReleaseException(String identifier) {
		super("file: \"" + identifier + "\"");
	}
}
