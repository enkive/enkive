package com.linuxbox.enkive.message.search.mongodb;


public class EmptySearchResultsException extends Exception {
	private static final long serialVersionUID = 3693326782924817237L;

	public EmptySearchResultsException(String message) {
		super(message);
	}

	public EmptySearchResultsException(Exception e) {
		super(e);
	}

	public EmptySearchResultsException(String message, Exception e) {
		super(message, e);
	}
}
