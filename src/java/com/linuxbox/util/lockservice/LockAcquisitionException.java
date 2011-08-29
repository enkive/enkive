package com.linuxbox.util.lockservice;

public class LockAcquisitionException extends LockServiceException {
	private static final long serialVersionUID = 8481271470965024607L;

	public LockAcquisitionException(String identifier, String message) {
		super(describe(identifier, message));
	}

	public LockAcquisitionException(String identifier, Throwable e) {
		super(describe(identifier, null), e);
	}

	private static String describe(String identifier, String message) {
		final String lockInfo = "lock: \"" + identifier + "\"";
		if (message == null) {
			return lockInfo;
		} else {
			return message + "; " + lockInfo;
		}
	}
}
