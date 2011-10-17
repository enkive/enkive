package com.linuxbox.enkive.message.search.exception;

public class MessageSearchException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6992009833531793302L;

	public MessageSearchException(String message) {
		super(message);
	}

	public MessageSearchException(String message, Throwable t) {
		super(message, t);
	}

	public MessageSearchException(Throwable t) {
		super(t);
	}
}
