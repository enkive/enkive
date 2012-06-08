package com.linuxbox.enkive.statistics.granularity;

import java.util.Calendar;
import java.util.Date;

public enum Granularity {
	HOURLY(0, -1, -1, -1), DAILY(-1, 0, -1, -1), MONTHLY(-1, 0, 1, -1), ANNUALLY(-1, 0, 1, 0);

	/*
	 * -1 = don't care; otherwise the timestamp has to have this particular
	 * value
	 */
	private int min;
	private int hour;
	private int dayOfMonth;
	private int month;

	Granularity(int min, int hour, int dayOfMonth, int month) {
		this.min = min;
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
		if (min >= 0 && min != c.get(Calendar.MINUTE)){
			System.out.println("min fails: ");
			System.out.println(min + " vs " + c.get(Calendar.MINUTE));
			return false;
		}
		
		if (hour >= 0 && hour != c.get(Calendar.HOUR_OF_DAY)) {
			System.out.println("hour fails: ");
			System.out.println(hour + " vs " + c.get(Calendar.HOUR_OF_DAY));
			return false;
		}

		if (dayOfMonth >= 0 && dayOfMonth != c.get(Calendar.DAY_OF_MONTH)) {
			System.out.println("day fails: ");
			System.out.println(dayOfMonth + " vs "
					+ c.get(Calendar.DAY_OF_MONTH));
			return false;
		}

		if (month >= 0 && month != c.get(Calendar.MONTH)) {
			System.out.println("month fails: ");
			System.out.println(month + " vs " + c.get(Calendar.MONTH));
			return false;
		}

		return true;
	}

	/**
	 * Some testing code; days are 1 based and months are 0 based
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Date t = new Date(2011, 1, 13, 17, 4, 32);
		Date t = new Date(111, 0, 1, 0, 0, 32);
		System.out.println("Date is: " + t);
		System.out.println("HOURLY " + HOURLY.isMatch(t));
		System.out.println("DAILY " + DAILY.isMatch(t));
		System.out.println("MONTHLY " + MONTHLY.isMatch(t));
		System.out.println("ANNUALLY " + ANNUALLY.isMatch(t));
	}
}
