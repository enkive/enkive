package com.linuxbox.enkive.statistics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StatsReportLogWriter {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics");

	StatsClient statsGatherer;

	public StatsReportLogWriter(StatsClient statsGatherer) {
		this.statsGatherer = statsGatherer;
	}

	public void logReport() {
		LOGGER.info(statsGatherer.gatherData());
	}

}
