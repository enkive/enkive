package com.linuxbox.enkive.docstore.mongogrid;

public interface Constants {
	// file control
	String CONT_FILE_COLLECTION = "fileControl";
	String CONT_FILE_IDENTIFIER_KEY = "identifier";
	String CONT_FILE_COUNT_KEY = "counter";
	String CONT_FILE_TIMESTAMP_KEY = "timestamp";
	
	// used by GridFS
	String OBJECT_ID_KEY = "_id";
	String FILENAME_KEY = "filename";
	String METADATA_KEY = "metadata";
	String MIME_TYPE_KEY = "contentType";

	// our metadata fields
	String BINARY_ENCODING_KEY = "binaryEncoding";
	String FILE_EXTENSION_KEY = "fileExtension";
	String INDEX_STATUS_KEY = "indexStatus";
	String INDEX_TIMESTAMP_KEY = "indexTimestamp";

	// our metadata fields with dot notation for queries
	String BINARY_ENCODING_QUERY = METADATA_KEY + "." + BINARY_ENCODING_KEY;
	String FILE_EXTENSION_QUERY = METADATA_KEY + "." + FILE_EXTENSION_KEY;
	String INDEX_STATUS_QUERY = METADATA_KEY + "." + INDEX_STATUS_KEY;
	String INDEX_TIMESTAMP_QUERY = METADATA_KEY + "." + INDEX_TIMESTAMP_KEY;
}
