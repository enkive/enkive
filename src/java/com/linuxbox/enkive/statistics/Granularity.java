package com.linuxbox.enkive.statistics;

import java.util.Calendar;
import java.util.Date;

public enum Granularity {
	HOURLY(-1, -1, -1), DAILY(0, -1, -1), MONTHLY(0, 1, -1), ANNUALLY(0, 1, 1);

	/*
	 * -1 = don't care; otherwise the timestamp has to have this particular
	 * value
	 */
	private int hour;
	private int dayOfMonth;
	private int month;

	Granularity(int hour, int dayOfMonth, int month) {
		this.hour = hour;
		this.dayOfMonth = dayOfMonth;
		this.month = month;
	}

	/**
	 * Return true if the given timestamp matches the granularity in terms of
	 * hour, day of month, and month.
	 * 
	 * @param timestamp
	 * @return
	 */
	public boolean isMatch(Date timestamp) {
		final Calendar c = Calendar.getInstance();
		c.setTime(timestamp);

		if (hour >= 0 && hour != c.get(Calendar.HOUR_OF_DAY)) {
			return false;
		}

		if (dayOfMonth >= 0 && dayOfMonth != c.get(Calendar.DAY_OF_MONTH)) {
			return false;
		}

		if (month >= 0 && month != c.get(Calendar.MONTH)) {
			return false;
		}

		return true;
	}

	/**
	 * Some testing code; are days of month and months 0-based or 1-based?
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Date t = new Date(2011, 1, 13, 17, 4, 32);
		System.out.println(HOURLY.isMatch(t));
		System.out.println(DAILY.isMatch(t));
		System.out.println(MONTHLY.isMatch(t));
		System.out.println(ANNUALLY.isMatch(t));
	}
}
