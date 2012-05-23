package com.linuxbox.enkive.statistics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.services.StatsGathererService;

public class StatsReportLogWriter {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics");

	StatsGathererService gatherer;

	public StatsReportLogWriter(StatsGathererService gather) {
		this.gatherer = gather;
	}

	public void logReport() {
		LOGGER.info(gatherer.gatherStats());
	}
}
