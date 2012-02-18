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
package com.linuxbox.enkive.docstore.mongogrid;

import java.nio.charset.Charset;

import com.linuxbox.util.mongodb.MongoDBConstants;

public interface Constants extends MongoDBConstants {
	Charset PREFERRED_CHARSET = Charset.forName("UTF-8");

	// file control
	String CONT_FILE_COLLECTION = "fileControl";
	String CONT_FILE_IDENTIFIER_KEY = "identifier";
	String CONT_FILE_TIMESTAMP_KEY = "timestamp";

	// our metadata fields
	String BINARY_ENCODING_KEY = "binaryEncoding";
	String FILE_EXTENSION_KEY = "fileExtension";
	String INDEX_STATUS_KEY = "indexStatus";
	String INDEX_TIMESTAMP_KEY = "indexTimestamp";
	String INDEX_SHARD_KEY = "indexShardKey";

	// our metadata fields with dot notation for queries
	String BINARY_ENCODING_QUERY = METADATA_KEY + "." + BINARY_ENCODING_KEY;
	String FILE_EXTENSION_QUERY = METADATA_KEY + "." + FILE_EXTENSION_KEY;
	String INDEX_STATUS_QUERY = METADATA_KEY + "." + INDEX_STATUS_KEY;
	String INDEX_TIMESTAMP_QUERY = METADATA_KEY + "." + INDEX_TIMESTAMP_KEY;
	String INDEX_SHARD_QUERY = METADATA_KEY + "." + INDEX_SHARD_KEY;
}
