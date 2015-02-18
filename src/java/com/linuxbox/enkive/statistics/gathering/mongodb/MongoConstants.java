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
package com.linuxbox.enkive.statistics.gathering.mongodb;

public interface MongoConstants {
	public static String MONGO_ID = "_id";
	public static String MONGO_AVG_OBJ_SIZE = "avgObjSize";
	public static String MONGO_COUNT = "count";

	public static String MONGO_DATA_SIZE = "dataSize";
	// DBs
	public static String MONGO_FILE_SIZE = "fileSize";
	public static String MONGO_INDEX_SIZE = "indexSize";
	public static String MONGO_INDEX_SIZES = "indexSizes";
	public static String MONGO_INDEXES = "indexes";
	public static String MONGO_LAST_EXTENT_SIZE = "lastExtentSize";
	public static String MONGO_LENGTH = "length";
	public static String MONGO_NAME = "db";

	// Collections
	public static String MONGO_NS = "ns";
	public static String MONGO_NUM_COLLECTIONS = "collections";
	public static String MONGO_NUM_EXTENT = "numExtents";
	public static String MONGO_NUM_INDEX = "nindexes";
	public static String MONGO_NUM_OBJS = "objects";
	public static String MONGO_SIZE = "size";
	// DBs and Colls
	public static String MONGO_STORAGE_SIZE = "storageSize";

	public static String MONGO_TOTAL_INDEX_SIZE = "totalIndexSize";
	// Attachments
	public static String MONGO_UPLOAD_DATE = "uploadDate";

	// msgs
	public static String MONGO_ARCHIVE_TIME = "archive_time";

}
