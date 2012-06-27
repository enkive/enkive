package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;

import java.util.List;

import com.linuxbox.enkive.statistics.KeyDef;

public class GathererAttributes {
	protected List<KeyDef> keys;
	protected String schedule;
	protected String serviceName;

	public GathererAttributes(String serviceName, String schedule,
			List<KeyDef> keys) {
		this.serviceName = serviceName;
		this.schedule = schedule;
		this.keys = keys;

		keys.add(new KeyDef(STAT_SERVICE_NAME + ":"));
		keys.add(new KeyDef(STAT_TIME_STAMP + ":" + GRAIN_MAX + "," + GRAIN_MIN));
	}

	public List<KeyDef> getKeys() {
		return keys;
	}

	public String getName() {
		return serviceName;
	}

	public String getSchedule() {
		return schedule;
	}
}
