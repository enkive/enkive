package com.linuxbox.enkive.statistics;

import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;

import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.services.StatsGathererService;

public class StatsReportLogWriter {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics");

	StatsGathererService gatherer;

	public StatsReportLogWriter(StatsGathererService gather) {
		this.gatherer = gather;
	}

	public void logReport() throws ParseException, SchedulerException, GathererException {
		LOGGER.info(gatherer.gatherStats());
	}
}
