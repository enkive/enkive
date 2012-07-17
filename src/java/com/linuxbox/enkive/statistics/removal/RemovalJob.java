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

//NOAH: I think the name for this class could be a little more descriptive. '
//This appears to be a class meant to be run by a job, so saying that in the class name 
//would make people weary of using it in any other way.

public class RemovalJob {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.removal");
	StatsClient client;
	private Date dateFilter;
	private int dayKeepTime = REMOVAL_DAY_ID;
	private int hrKeepTime = REMOVAL_HOUR_ID;
	private int monthKeepTime = REMOVAL_MONTH_ID;
	private int rawKeepTime = REMOVAL_RAW_ID;
	private int wkKeepTime = REMOVAL_WEEK_ID;

	public RemovalJob(StatsClient client) {
		this.client = client;
	}

	/**
	 * Each cleaner is run to determine if it can find any data that should be
	 * deleted from the database at that particular time if the keepTime for a
	 * cleaner was set to -1 that cleaner will not run NOTE: keepTimes do not 
	 * need to be set; they have default values, and any overwrite must be
	 * greater than those default values
	 */
	public void cleanAll() {
		LOGGER.info("Starting removal");
		cleanRaw();
		cleanHour();
		cleanDay();
		cleanWeek();
		cleanMonth();
		LOGGER.info("Finished Removal");
	}

	private void cleanDay() {
		if (dayKeepTime != -1) {
			cleaner(REMOVAL_DAY_ID, GRAIN_DAY);
		}
	}

	/**
	 * this method takes an identifier that determines a date range and a type of grainualarity and deletes all
	 * the objects found in a query over these 
	 * @param interval - the identifier that determines how the date is to be made
	 * @param type - the type of grainularity to delete
	 */
	private void cleaner(int interval, int type) {
		setDate(interval);
		Set<Map<String, Object>> data = client.queryStatistics(null, new Date(
				0L), dateFilter);
		Set<Object> deletionSet = new HashSet<Object>();

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
		client.remove(deletionSet);
	}

	private void cleanHour() {
		if (hrKeepTime != -1) {
			cleaner(REMOVAL_HOUR_ID, GRAIN_HOUR);
		}
	}

	private void cleanMonth() {
		if (monthKeepTime != -1) {
			cleaner(REMOVAL_MONTH_ID, GRAIN_MONTH);
		}
	}

	private void cleanRaw() {
		if (rawKeepTime != -1) {
			cleaner(REMOVAL_RAW_ID, 0);
		}
	}

	private void cleanWeek() {
		if (wkKeepTime != -1) {
			cleaner(REMOVAL_WEEK_ID, GRAIN_WEEK);
		}
	}
	
	/**
	 * generates a date cooresponding to the amount of time each
	 * type of statistic should be kept. For instance, if monthKeepTime is set to 3 then 3
	 * months will be subtracted from the current date and that date will be returned
	 * @param time - the identifier corresponding to the amount of time to deduct
	 */
	private void setDate(int time) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);

		switch (time) {
		case REMOVAL_MONTH_ID:// month
			cal.add(Calendar.MONTH, -monthKeepTime);
			break;
		case REMOVAL_WEEK_ID:// week
			cal.add(Calendar.WEEK_OF_YEAR, -wkKeepTime);
			break;
		case REMOVAL_DAY_ID:// day
			cal.add(Calendar.DATE, -dayKeepTime);
			break;
		case REMOVAL_HOUR_ID:// hour
			cal.add(Calendar.HOUR, -hrKeepTime);
			break;
		case REMOVAL_RAW_ID:// raw
			cal.add(Calendar.HOUR, -rawKeepTime);
		}
		dateFilter = cal.getTime();
	}

	/**
	 * @param dayKeepTime the input must be greater than the default value or -1
	 * (-1 means do not run this removal)
	 */
	public void setDayKeepTime(int dayKeepTime) {
		if (dayKeepTime >= REMOVAL_DAY_ID || dayKeepTime == -1) {
			this.dayKeepTime = dayKeepTime;
		} else {
			LOGGER.warn("setDayKeepTime input is invalid");
		}
	}

	/**
	 * @param hrKeepTime the input must be greater than the default value or -1
	 * (-1 means do not run this removal)
	 */
	public void setHrKeepTime(int hrKeepTime) {
		if (hrKeepTime >= REMOVAL_HOUR_ID || hrKeepTime == -1) {
			this.hrKeepTime = hrKeepTime;
		} else {
			LOGGER.warn("setHrKeepTime input is invalid");
		}
	}

	/**
	 * @param monthKeepTime the input must be greater than the default value or -1
	 * (-1 means do not run this removal)
	 */
	public void setMonthKeepTime(int monthKeepTime) {
		if (monthKeepTime >= REMOVAL_MONTH_ID || monthKeepTime == -1) {
			this.monthKeepTime = monthKeepTime;
		} else {
			LOGGER.warn("setMonthKeepTime input is invalid");
		}
	}
	
	/**
	 * @param rawKeepTime the input must be greater than the default value or -1
	 * (-1 means do not run this removal)
	 */
	public void setRawKeepTime(int rawKeepTime) {
		if (rawKeepTime >= REMOVAL_RAW_ID || rawKeepTime == -1) {
			this.rawKeepTime = rawKeepTime;
		} else {
			LOGGER.warn("setRawKeepTime input is invalid");
		}
	}

	/**
	 * @param wkKeepTime the input must be greater than the default value or -1
	 * (-1 means do not run this removal)
	 */
	public void setWkKeepTime(int wkKeepTime) {
		if (wkKeepTime >= REMOVAL_WEEK_ID || wkKeepTime == -1) {
			this.wkKeepTime = wkKeepTime;
		} else {
			LOGGER.warn("setWkKeepTime input is invalid");
		}
	}
}
