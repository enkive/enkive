package com.linuxbox.enkive.archiver.mongodb;

public interface MongoMessageStoreConstants {

	String MESSAGE_UUID = "_id";
	String ARCHIVE_TIME = "archive_time";
	String ATTACHMENT_ID = "attachment_id";
	String ATTACHMENT_ID_LIST = "attachment_ids";
	String NESTED_MESSAGE_ID_LIST = "nested_message_ids";
	String CONTENT_HEADER = "content_header";
	String CONTENT_HEADER_TYPE = "type";
	String MULTIPART_HEADER_TYPE = "multiPartHeader";
	String SINGLE_PART_HEADER_TYPE = "singlePartHeader";
	String PART_HEADERS = "partHeaders";

}
