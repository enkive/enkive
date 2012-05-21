package com.linuxbox.enkive.statistics.gathering;

public class StatsGatherException extends Exception {

	private static final long serialVersionUID = 1L;

	public StatsGatherException(String m) {
		super(m);
	}

	public StatsGatherException(Throwable t) {
		super(t);
	}

	public StatsGatherException(String m, Throwable t) {
		super(m, t);
	}
}
