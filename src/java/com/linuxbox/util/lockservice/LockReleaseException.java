package com.linuxbox.util.lockservice;

public class LockReleaseException extends LockServiceException {
	private static final long serialVersionUID = 5646914906405589115L;

	public LockReleaseException(String identifier) {
		super("file: \"" + identifier + "\"");
	}
}
