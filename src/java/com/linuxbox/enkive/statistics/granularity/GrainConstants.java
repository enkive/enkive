package com.linuxbox.enkive.statistics.granularity;

public class GrainConstants {
	
	public static int GRAIN_RAW   = 0;
	public static int GRAIN_HOUR  = 1;
	public static int GRAIN_DAY   = 1*24;
	public static int GRAIN_WEEK  = 1*24*7;
	public static int GRAIN_MONTH = 1*24*30;
	
	public static String GRAIN_AVG = "avg";
	public static String GRAIN_MAX = "max";
	public static String GRAIN_MIN = "min";
	public static String GRAIN_SUM = "sum";
	public static String GRAIN_TYPE = "gTyp";
	public static String GRAIN_WEIGHT = "wgt";
	public static String GRAIN_STD_DEV = "std";
}
