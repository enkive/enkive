package com.linuxbox.enkive.statistics;

import java.text.SimpleDateFormat;

public interface StatsConstants {
	public static String QUEUE_LENGTH = "QueueLength";
	public static String ARCHIVE_SIZE = "MessagesInArchive";
	public static String STATISTIC_CHECK_ERROR = "CheckError";
	
//key names
	//DBs only
	public static String STAT_FILE_SIZE = "fileSize";
	public static String STAT_NUM_COLLECTIONS = "numCollections";
	
	//Colls only
	public static String STAT_NS = "ns";
	public static String STAT_INDEX_SIZES = "indexSizes";
	public static String STAT_LAST_EXTENT_SIZE = "lastExtentSize";
	
	//Runtime only
	public static String STAT_MAX_MEMORY   = "maxMemory";
	public static String STAT_FREE_MEMORY  = "freeMemory";
	public static String STAT_TOTAL_MEMORY = "totalMemory";
	public static String STAT_PROCESSORS   = "availableProcessors";
	
	//DBs & Colls
	public static String STAT_NAME = "name";
	public static String STAT_TYPE = "type";//runtime also has a 'type' field
	public static String STAT_NUM_OBJS = "numObjs";
	public static String STAT_DATA_SIZE = "dataSize";
	public static String STAT_NUM_INDEX = "numIndexes";
	public static String STAT_NUM_EXTENT = "numExtents";
	public static String STAT_TOTAL_SIZE = "storageSize";	
	public static String STAT_AVG_OBJ_SIZE = "avgObjSize";
	public static String STAT_TOTAL_INDEX_SIZE = "totalIndexSize";
	
	//MsgEntries
	public static String STAT_NUM_ENTRIES = "NumOfEntries";
	
	//ErrorKey
	public static String STAT_ERROR = "ERROR";
//end of keynames
	
	
	//misc
	public static long THIRTY_DAYS = 2592000000L;//millisecond value of 30 days
	//don't forget  MM starts index at jan = 01 (not 00)
	public static SimpleDateFormat SIMPLE_DATE = new SimpleDateFormat("yyyy-MM-dd");
}