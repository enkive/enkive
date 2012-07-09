package com.linuxbox.enkive.statistics.granularity;

import java.util.Calendar;
import java.util.Date;

public enum Granularity {
	DAY(0, -1, -1, -1), // any hour (hourly controlled by cronTrigger)
	HOUR(-1, -1, -1, -1), // first hour of any day
	MONTH(-1, 1, -1, -1), // first day of any week
	WEEK(-1, -1, 1, -1); // first day of any month
	public static void main(String[] args) {
		// Date t = new Date(2011, 1, 13, 17, 4, 32);
		System.out.println("CurrDate: " + new Date());
		System.out.println("HOUR " + HOUR.isMatch());
		System.out.println("DAY " + DAY.isMatch());
		System.out.println("WEEK " + WEEK.isMatch());
		System.out.println("MONTH " + MONTH.isMatch());
	}

	private int dayOfMonth;
	// -1 means any
	private int hour;
	private int month;

	private int week;

	Granularity(int hour, int dayOfMonth, int week, int month) {
		this.hour = hour;
		this.dayOfMonth = dayOfMonth;
		this.week = week;
		this.month = month;
	}

	public boolean isMatch() {
		final Calendar c = Calendar.getInstance();

		if (hour >= 0 && hour != c.get(Calendar.HOUR_OF_DAY)) {
			return false;
		}

		if (dayOfMonth >= 0 && dayOfMonth != c.get(Calendar.DAY_OF_MONTH)) {
			return false;
		}

		if (week >= 0 && c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
				&& hour != c.get(Calendar.HOUR_OF_DAY)) {
			return false;
		}

		if (month >= 0 && month != c.get(Calendar.MONTH)) {
			return false;
		}

		return true;
	}
}
