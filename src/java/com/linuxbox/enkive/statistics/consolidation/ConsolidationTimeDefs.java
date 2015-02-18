/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.statistics.consolidation;

import java.util.Calendar;

public enum ConsolidationTimeDefs {
	HOUR(-1, -1, -1, -1), // any hour (hourly controlled by cronTrigger)
	DAY(0, -1, -1, -1), // first hour of any day
	WEEK(0, -1, 1, -1), // Sunday of any week
	MONTH(0, 1, -1, -1); // first day of any month

	private int hour;
	private int dayOfMonth;// -1 means any
	private int week;
	private int month;

	ConsolidationTimeDefs(int hour, int dayOfMonth, int week, int month) {
		this.hour = hour;
		this.dayOfMonth = dayOfMonth;
		this.week = week;
		this.month = month;
	}

	public boolean isMatch() {
		final Calendar c = Calendar.getInstance();
		return isMatch(c);
	}

	public boolean isMatch(Calendar c) {
		if (hour >= 0 && hour != c.get(Calendar.HOUR_OF_DAY)) {
			return false;
		}

		if (dayOfMonth >= 0 && dayOfMonth != c.get(Calendar.DAY_OF_MONTH)) {
			return false;
		}

		if (week >= 0 && c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			return false;
		}

		if (month >= 0 && month != c.get(Calendar.MONTH)) {
			return false;
		}

		return true;
	}
}
