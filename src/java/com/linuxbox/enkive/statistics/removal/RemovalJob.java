/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.linuxbox.enkive.statistics.removal;

import static com.linuxbox.enkive.statistics.VarsMaker.createSetOfObjs;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_DAY;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_HOUR;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MONTH;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_RAW;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_TYPE;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_WEEK;
import static com.linuxbox.enkive.statistics.removal.RemovalConstants.REMOVAL_DAY_ID;
import static com.linuxbox.enkive.statistics.removal.RemovalConstants.REMOVAL_HOUR_ID;
import static com.linuxbox.enkive.statistics.removal.RemovalConstants.REMOVAL_MONTH_ID;
import static com.linuxbox.enkive.statistics.removal.RemovalConstants.REMOVAL_RAW_ID;
import static com.linuxbox.enkive.statistics.removal.RemovalConstants.REMOVAL_WEEK_ID;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.retrieval.StatsQuery;
import com.linuxbox.enkive.statistics.services.retrieval.mongodb.MongoStatsDateQuery;

public class RemovalJob {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.removal");
	StatsClient client;
	private Date dateFilter;
	private int dayKeepTime = REMOVAL_DAY_ID;
	private int hourKeepTime = REMOVAL_HOUR_ID;
	private int monthKeepTime = REMOVAL_MONTH_ID;
	private int rawKeepTime = REMOVAL_RAW_ID;
	private int weekKeepTime = REMOVAL_WEEK_ID;

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
			cleaner(REMOVAL_DAY_ID, CONSOLIDATION_DAY);
		}
	}

	/**
	 * this method takes an identifier that determines a date range and a type
	 * of grainualarity and deletes all the objects found in a query over these
	 * 
	 * @param interval
	 *            - the identifier that determines how the date is to be made
	 * @param grainType
	 *            - the type of grainularity to delete
	 */
	private void cleaner(int interval, int grainType) {
		setDate(interval);
		StatsQuery query = new MongoStatsDateQuery(new Date(0L), dateFilter);
		query.grainType = grainType;
		Set<Map<String, Object>> data = client.queryStatistics(query);
		Set<Object> deletionSet = createSetOfObjs();

		for (Map<String, Object> map : data) {
			Integer gType = (Integer) map.get(CONSOLIDATION_TYPE);
			if (gType != null) {
				if (gType.equals(grainType)) {
					deletionSet.add(map.get("_id"));
				}
			} else if (grainType == 0) {
				deletionSet.add(map.get("_id"));
			}
		}
		client.remove(deletionSet);
	}

	private void cleanHour() {
		if (hourKeepTime != -1) {
			cleaner(REMOVAL_HOUR_ID, CONSOLIDATION_HOUR);
		}
	}

	private void cleanMonth() {
		if (monthKeepTime != -1) {
			cleaner(REMOVAL_MONTH_ID, CONSOLIDATION_MONTH);
		}
	}

	private void cleanRaw() {
		if (rawKeepTime != -1) {
			cleaner(REMOVAL_RAW_ID, CONSOLIDATION_RAW);
		}
	}

	private void cleanWeek() {
		if (weekKeepTime != -1) {
			cleaner(REMOVAL_WEEK_ID, CONSOLIDATION_WEEK);
		}
	}

	/**
	 * generates a date cooresponding to the amount of time each type of
	 * statistic should be kept. For instance, if monthKeepTime is set to 3 then
	 * 3 months will be subtracted from the current date and that date will be
	 * returned
	 * 
	 * @param time
	 *            - the identifier corresponding to the amount of time to deduct
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
			cal.add(Calendar.WEEK_OF_YEAR, -weekKeepTime);
			break;
		case REMOVAL_DAY_ID:// day
			cal.add(Calendar.DATE, -dayKeepTime);
			break;
		case REMOVAL_HOUR_ID:// hour
			cal.add(Calendar.HOUR, -hourKeepTime);
			break;
		case REMOVAL_RAW_ID:// raw
			cal.add(Calendar.HOUR, -rawKeepTime);
		}
		dateFilter = cal.getTime();
	}

	/**
	 * @param dayKeepTime
	 *            the input must be greater than the default value or -1 (-1
	 *            means do not run this removal)
	 */
	public void setDayKeepTime(int dayKeepTime) {
		if (dayKeepTime >= REMOVAL_DAY_ID || dayKeepTime == -1) {
			this.dayKeepTime = dayKeepTime;
		} else {
			LOGGER.warn("setDayKeepTime input is invalid");
		}
	}

	/**
	 * @param hrKeepTime
	 *            the input must be greater than the default value or -1 (-1
	 *            means do not run this removal)
	 */
	public void setHourKeepTime(int hourKeepTime) {
		if (hourKeepTime >= REMOVAL_HOUR_ID || hourKeepTime == -1) {
			this.hourKeepTime = hourKeepTime;
		} else {
			LOGGER.warn("setHrKeepTime input is invalid");
		}
	}

	/**
	 * @param monthKeepTime
	 *            the input must be greater than the default value or -1 (-1
	 *            means do not run this removal)
	 */
	public void setMonthKeepTime(int monthKeepTime) {
		if (monthKeepTime >= REMOVAL_MONTH_ID || monthKeepTime == -1) {
			this.monthKeepTime = monthKeepTime;
		} else {
			LOGGER.warn("setMonthKeepTime input is invalid");
		}
	}

	/**
	 * @param rawKeepTime
	 *            the input must be greater than the default value or -1 (-1
	 *            means do not run this removal)
	 */
	public void setRawKeepTime(int rawKeepTime) {
		if (rawKeepTime >= REMOVAL_RAW_ID || rawKeepTime == -1) {
			this.rawKeepTime = rawKeepTime;
		} else {
			LOGGER.warn("setRawKeepTime input is invalid");
		}
	}

	/**
	 * @param wkKeepTime
	 *            the input must be greater than the default value or -1 (-1
	 *            means do not run this removal)
	 */
	public void setWeekKeepTime(int weekKeepTime) {
		if (weekKeepTime >= REMOVAL_WEEK_ID || weekKeepTime == -1) {
			this.weekKeepTime = weekKeepTime;
		} else {
			LOGGER.warn("setWkKeepTime input is invalid");
		}
	}
}
