package com.linuxbox.enkive.statistics.services.retrieval;

public class StatsRetrievalException extends Exception {

	private static final long serialVersionUID = 1L;

	public StatsRetrievalException(String m) {
		super(m);
	}

	public StatsRetrievalException(String m, Throwable t) {
		super(m, t);
	}

	public StatsRetrievalException(Throwable t) {
		super(t);
	}
}
