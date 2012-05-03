package com.linuxbox.enkive.statistics;

public interface StatisticsConstants {
	public static String QUEUE_LENGTH = "QueueLength";
	public static String ARCHIVE_SIZE = "MessagesInArchive";
	public static String STATISTIC_CHECK_ERROR = "CheckError";
	
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
	public static String STAT_TYPE = "type";
	public static String STAT_NUM_OBJS = "numObjs";
	public static String STAT_DATA_SIZE = "dataSize";
	public static String STAT_NUM_INDEX = "numIndexs";
	public static String STAT_NUM_EXTENT = "numExtents";
	public static String STAT_TOTAL_SIZE = "storageSize";	
	public static String STAT_AVG_OBJ_SIZE = "avgObjSize";
	public static String STAT_TOTAL_INDEX_SIZE = "totalIndexSize";
}

/*
    Database Properties: 

	type - 'database'
    name - name of database
    numCollections - number of collections
    numObjs - number of documents
    avgObjSize - average object size
    dataSize - size of database (used)
    storageSize - total amount of memory allocated
    numExtents - number of extents
    numIndexes - number of indexes
    totalIndexSize - total size of indexes
    fileSize - total size of the data files that store the database

    Collection Properties: 

	type - 'collection'
	name - name of collection
    ns - namespace
    numObjs - number of documents
    avgObjSize - average object size
    dataSize - collection size
    storageSize - preallocated memory size
    numExtents - number of extents
    lastExtentSize - size of the last extent
    numIndexes - number of indexes
    totalIndexSize - total index size
    indexSizes - index sizes stored in this format: {"indexName", size...} 

    Runtime Properties: 

	type - 'runtime'
    maxMemory - maximum amount of memory the java virtual machine will attemp to use
    freeMemory  - amount of free memory in the java virtual machine
    totalMemory - total memory in the java virtual machine
    availableProcessors - the number of processors available to teh java virtual machine
 */