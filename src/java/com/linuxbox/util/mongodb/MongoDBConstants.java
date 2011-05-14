package com.linuxbox.util.mongodb;

public interface MongoDBConstants {
	// Mongo internals
	int DUPLICATE_KEY_ERROR_CODE = 11000;

	// used by GridFS
	String OBJECT_ID_KEY = "_id";
	String FILENAME_KEY = "filename";
	String METADATA_KEY = "metadata";
	String MIME_TYPE_KEY = "contentType";
	String GRID_FS_FILES_COLLECTION_SUFFIX = ".files";
	String GRID_FS_CHUNKS_COLLECTION_SUFFIX = ".chunks";
}
