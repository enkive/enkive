package com.linuxbox.enkive.exception;

public class CannotGetPermissionsException extends EnkiveException {
	private static final long serialVersionUID = 1L;

	public CannotGetPermissionsException() {
		super();
	}

	public CannotGetPermissionsException(String message, Throwable cause) {
		super(message, cause);
	}

	public CannotGetPermissionsException(String message) {
		super(message);
	}

	public CannotGetPermissionsException(Throwable cause) {
		super(cause);
	}
}
