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

	// Calling ensureIndex launch can be a bad idea if the index needs to be
	// created; it could stop the system from running for hours (or days). A
	// better practice is to run ensureIndex as part of system administration,
	// choosing when to run it and whether to run it in the background. Rather
	// than completely delete the orignal code that called ensureIndex, we'll
	// just put it in a conditional on this constant. That way, the expectations
	// are at least documented.
	boolean CALL_ENSURE_INDEX_ON_INIT = false;
}
