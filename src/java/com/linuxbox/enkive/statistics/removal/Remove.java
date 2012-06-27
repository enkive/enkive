package com.linuxbox.enkive.statistics.removal;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_DAY;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_HOUR;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MONTH;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_WEEK;
import static com.linuxbox.enkive.statistics.removal.RemovalConstants.REMOVAL_DAY_ID;
import static com.linuxbox.enkive.statistics.removal.RemovalConstants.REMOVAL_HOUR_ID;
import static com.linuxbox.enkive.statistics.removal.RemovalConstants.REMOVAL_MONTH_ID;
import static com.linuxbox.enkive.statistics.removal.RemovalConstants.REMOVAL_RAW_ID;
import static com.linuxbox.enkive.statistics.removal.RemovalConstants.REMOVAL_WEEK_ID;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.services.StatsClient;

public class Remove {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.removal");
	StatsClient client;
	private Date dateFilter;
	private int dayBuff = REMOVAL_DAY_ID;
	private int hrBuff = REMOVAL_HOUR_ID;
	private int monthBuff = REMOVAL_MONTH_ID;
	private int rawBuff = REMOVAL_RAW_ID;
	private int wkBuff = REMOVAL_WEEK_ID;

	public Remove(StatsClient client) {
		this.client = client;
	}

	public void cleanAll() {
		LOGGER.info("Starting removal");
		Calendar c = Calendar.getInstance();
		cleanRaw();
		cleanHour();

		if (c.get(Calendar.HOUR_OF_DAY) == 0) {
			System.out.println("Day");
			cleanDay();

			if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				cleanWeek();
			}

			if (c.get(Calendar.DAY_OF_MONTH) == 1) {
				cleanMonth();
			}
		}
		LOGGER.info("Finished Removal");
	}

	private void cleanDay() {
		if (dayBuff != -1) {
			cleaner(REMOVAL_DAY_ID, GRAIN_DAY);
		}
	}

	private void cleaner(int interval, int type) {
		setDate(interval);
		Set<Map<String, Object>> data = client.queryStatistics(null, new Date(
				0L), dateFilter);
		Set<Object> deletionSet = new HashSet<Object>();
		// TODO
		System.out.println("#items in COLL: "
				+ client.queryStatistics(null, null, null).size());

		for (Map<String, Object> map : data) {
			Integer gType = (Integer) map.get(GRAIN_TYPE);
			if (gType != null) {
				if (gType.equals(type)) {
					deletionSet.add(map.get("_id"));
				}
			} else if (type == 0) {
				deletionSet.add(map.get("_id"));
			}
		}
		// TODO
		System.out.println("#items to delete: " + deletionSet.size());
		client.remove(deletionSet);
		System.out.println("#items in COLL after: "
				+ client.queryStatistics(null, null, null).size());
	}

	private void cleanHour() {
		if (hrBuff != -1) {
			cleaner(REMOVAL_HOUR_ID, GRAIN_HOUR);
		}
	}

	private void cleanMonth() {
		if (monthBuff != -1) {
			cleaner(REMOVAL_MONTH_ID, GRAIN_MONTH);
		}
	}

	private void cleanRaw() {
		if (rawBuff != -1) {
			cleaner(REMOVAL_RAW_ID, 0);
		}
	}

	private void cleanWeek() {
		if (wkBuff != -1) {
			cleaner(REMOVAL_WEEK_ID, GRAIN_WEEK);
		}
	}

	private void setDate(int time) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);

		switch (time) {
		case REMOVAL_MONTH_ID:// month
			cal.add(Calendar.MONTH, -monthBuff);
			break;
		case REMOVAL_WEEK_ID:// week
			cal.add(Calendar.WEEK_OF_YEAR, -wkBuff);
			break;
		case REMOVAL_DAY_ID:// day
			cal.add(Calendar.DATE, -dayBuff);
			break;
		case REMOVAL_HOUR_ID:// hour
			cal.add(Calendar.HOUR, -hrBuff);
			break;
		case REMOVAL_RAW_ID:// raw
			cal.add(Calendar.HOUR, -rawBuff);
		}
		dateFilter = cal.getTime();
	}

	public void setDayBuff(int dayBuff) {
		if (dayBuff >= REMOVAL_DAY_ID || dayBuff == -1) {
			this.dayBuff = dayBuff;
		} else {
			LOGGER.warn("setDayBuff input is invalid");
		}
	}

	public void setHrBuff(int hrBuff) {
		if (hrBuff >= REMOVAL_HOUR_ID || hrBuff == -1) {
			this.hrBuff = hrBuff;
		} else {
			LOGGER.warn("setHrBuff input is invalid");
		}
	}

	public void setMonthBuff(int monthBuff) {
		if (monthBuff >= REMOVAL_MONTH_ID || monthBuff == -1) {
			this.monthBuff = monthBuff;
		} else {
			LOGGER.warn("setMonthBuff input is invalid");
		}
	}

	public void setRawBuff(int rawBuff) {
		if (rawBuff >= REMOVAL_RAW_ID || rawBuff == -1) {
			this.rawBuff = rawBuff;
		} else {
			LOGGER.warn("setRawBuff input is invalid");
		}
	}

	public void setWkBuff(int wkBuff) {
		if (wkBuff >= REMOVAL_WEEK_ID || wkBuff == -1) {
			this.wkBuff = wkBuff;
		} else {
			LOGGER.warn("setWkBuff input is invalid");
		}
	}
}
