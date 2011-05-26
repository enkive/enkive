package com.linuxbox.enkive.archiver;

public interface MesssageAttributeConstants {

	// Message Properties
	String MAIL_FROM = "mail_from";
	String RCPT_TO = "rcpt_to";
	String FROM = "from";
	String TO = "to";
	String CC = "cc";
	String DATE = "date";
	String SUBJECT = "subject";
	String MESSAGE_ID = "message_id";
	String MIME_VERSION = "mime_version";	
	String ORIGINAL_HEADERS = "original_headers";
	String MESSAGE_DIFF = "messageDiff";
	
	// Multipart Header Properties

	String BOUNDARY_ID = "boundary_id";
	String PREAMBLE = "preamble";
	String EPILOGUE = "epilogue";
	
	// Header Properties
	String CONTENT_ID = "content_id";
	String CONTENT_TYPE = "content_type";
	String CONTENT_DISPOSITION = "content_disposition";
	String FILENAME = "filename";
	String CONTENT_TRANSFER_ENCODING = "content_transfer_encoding";
	
}
