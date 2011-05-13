package com.linuxbox.util.queueservice;

public abstract class QueueServiceException extends Exception {
	private static final long serialVersionUID = -573320717381049441L;

	public QueueServiceException() {
		super();
	}

	public QueueServiceException(String message) {
		super(message);
	}

	public QueueServiceException(Throwable thrown) {
		super(thrown);
	}

	public QueueServiceException(String message, Throwable thrown) {
		super(message, thrown);
	}
}
