package com.linuxbox.enkive.statistics;

import java.text.SimpleDateFormat;

public interface StatsConstants {
	public static String QUEUE_LENGTH = "queLen";
	public static String ARCHIVE_SIZE = "MsgArchive";
	public static String STATISTIC_CHECK_ERROR = "CheckError";
	public static String STAT_TYPE_COLL = "COLL";
	public static String STAT_TYPE_DB = "DB";
	public static String STAT_TYPE_RUN = "RUN";
	
	
	// Attachments
	public static String STAT_MAX_ATTACH = "maxAtt";
	public static String STAT_AVG_ATTACH = "avgAtt";
	public static String STAT_UPLOAD_DATE = "upDate";
	public static String STAT_LENGTH = "l";
	
	// DBs only
	public static String STAT_FILE_SIZE = "fileSz";
	public static String STAT_NUM_COLLECTIONS = "nColls";

	// Colls only
	public static String STAT_NS = "ns";
	public static String STAT_INDEX_SIZES = "indSzs";
	public static String STAT_LAST_EXTENT_SIZE = "lExSz";

	// Runtime only
	public static String STAT_MAX_MEMORY = "maxM";
	public static String STAT_FREE_MEMORY = "freeM";
	public static String STAT_TOTAL_MEMORY = "totM";
	public static String STAT_PROCESSORS = "cores";

	// DBs & Colls
	public static String STAT_NAME = "db";
	public static String STAT_TYPE = "type";
	public static String STAT_TIME = "time";
	public static String STAT_NUM_OBJS = "numObj";
	public static String STAT_DATA_SIZE = "dataSz";
	public static String STAT_NUM_INDEX = "numInd";
	public static String STAT_NUM_EXTENT = "numExt";
	public static String STAT_TOTAL_SIZE = "totSz";
	public static String STAT_AVG_OBJ_SIZE = "avgOSz";
	public static String STAT_TOTAL_INDEX_SIZE = "indSz";

	// MsgEntries
	public static String STAT_NUM_ENTRIES = "numMsg";
	
	// ErrorKey
	public static String STAT_ERROR = "ERR";

	public static String STAT_STORAGE_DB = "enkive";
	public static String STAT_STORAGE_COLLECTION = "statistics";
	public static String STAT_SERVICE_NAME = "sn";
	public static String STAT_TIME_STAMP = "ts";
	// end of keynames

	// misc
	public static long THIRTY_DAYS = 2592000000L;// millisecond value of 30 days
	public static SimpleDateFormat SIMPLE_DATE = new SimpleDateFormat(
			"yyyy-MM-dd");

}