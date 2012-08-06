package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;

import java.text.ParseException;
import java.util.List;

import org.quartz.CronExpression;

import com.linuxbox.enkive.statistics.KeyConsolidationHandler;

public class GathererAttributes {
	protected List<KeyConsolidationHandler> keys;
	protected CronExpression schedule;
	protected String serviceName;

	public GathererAttributes(String serviceName, String schedule,
			List<KeyConsolidationHandler> keys) throws ParseException {
		this.serviceName = serviceName;
		this.schedule = new CronExpression(schedule);
		this.keys = keys;
		//serviceName and Timestamp must always be specified
		keys.add(new KeyConsolidationHandler(STAT_GATHERER_NAME + "::Gatherer Name:"));
		keys.add(new KeyConsolidationHandler(STAT_TIME_STAMP + ":" + GRAIN_MAX + "," + GRAIN_MIN + ":Time Stamp:"));
	}

	/**
	 * @return the consolidation handlers cooresponding to this gatherer
	 */
	public List<KeyConsolidationHandler> getKeys() {
		return keys;
	}

	/**
	 * @return the name of the gatherer this attributes class belongs to
	 */
	public String getName() {
		return serviceName;
	}

	/**
	 * @return the cronExpression schedule this gatherer runs on
	 */
	public CronExpression getSchedule() {
		return schedule;
	}
}
