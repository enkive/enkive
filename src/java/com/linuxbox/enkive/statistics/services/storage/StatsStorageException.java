package com.linuxbox.enkive.statistics.services.storage;

public class StatsStorageException extends Exception {
	private static final long serialVersionUID = 1L;

	public StatsStorageException(String m) {
		super(m);
	}

	public StatsStorageException(Throwable t) {
		super(t);
	}

	public StatsStorageException(String m, Throwable t) {
		super(m, t);
	}
}
