package com.linuxbox.enkive.statistics.gathering.mongodb;

public interface MongoConstants {
	//Attachments
	public static String MONGO_UPLOAD_DATE = "uploadDate";
	public static String MONGO_LENGTH = "length";

	//DBs
	public static String MONGO_FILE_SIZE = "fileSize";
	public static String MONGO_NUM_COLLECTIONS = "collections";
	public static String MONGO_NAME = "db";
	public static String MONGO_NUM_OBJS = "objects";
	public static String MONGO_AVG_OBJ_SIZE = "avgObjSize";
	public static String MONGO_DATA_SIZE = "dataSize";
	public static String MONGO_INDEXES = "indexes";
	public static String MONGO_INDEX_SIZE = "indexSize";
	
	//Collections
	public static String MONGO_NS = "ns";
	public static String MONGO_INDEX_SIZES = "indexSizes";
	public static String MONGO_LAST_EXTENT_SIZE = "lastExtentSize";
	public static String MONGO_SIZE = "size";
	public static String MONGO_COUNT = "count";
	public static String MONGO_NUM_INDEX = "nindexes";
	public static String MONGO_TOTAL_INDEX_SIZE = "totalIndexSize";
	
	//DBs and Colls
	public static String MONGO_STORAGE_SIZE = "storageSize";
	public static String MONGO_NUM_EXTENT = "numExtents";
	
}