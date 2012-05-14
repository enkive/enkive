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
 ******************************************************************************/
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
	String ATTACHMENT_ID_INDEX = "attachment_id_index";
	String NESTED_MESSAGE_ID_INDEX = "nested_msg_id_index";
}
