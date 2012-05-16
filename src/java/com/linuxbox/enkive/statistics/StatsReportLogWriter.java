package com.linuxbox.enkive.statistics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

public class StatsReportLogWriter {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics");

	StatsService statsGatherer;

	public StatsReportLogWriter(StatsService statsGatherer) {
		this.statsGatherer = statsGatherer;
	}

	public void logReport() {
		try {
			LOGGER.info(statsGatherer.getStatisticsJSON());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
