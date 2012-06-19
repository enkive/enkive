package com.linuxbox.enkive.statistics.removal;

public interface RemovalConstants {
	public static String METHOD = "cleanAll";
	
	//values assigned by how long you want to keep them (ie. 48 in the hour id means it is kept for 48 hours)
	public static int REMOVAL_RAW_ID = 1;
	public static int REMOVAL_HOUR_ID = 48;
	public static int REMOVAL_DAY_ID = 60;
	public static int REMOVAL_WEEK_ID = 10;
	public static int REMOVAL_MONTH_ID = 24;
}