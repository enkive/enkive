package com.linuxbox.enkive.statistics.gathering;

public class GathererException extends Exception {

	private static final long serialVersionUID = 1L;

	public GathererException(String m) {
		super(m);
	}

	public GathererException(String m, Throwable t) {
		super(m, t);
	}

	public GathererException(Throwable t) {
		super(t);
	}
}
