/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
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
 *******************************************************************************/
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
