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
package com.linuxbox.enkive.docstore.mongogrid;

import java.io.InputStream;

import com.linuxbox.enkive.docstore.AbstractDocument;
import com.mongodb.gridfs.GridFSDBFile;

public class MongoGridDocument extends AbstractDocument {
	private GridFSDBFile gridFile;

	public MongoGridDocument(GridFSDBFile gridFile) {
		super(gridFile.getContentType(), gridFile.getFilename(),
				(String) gridFile.getMetaData().get(
						Constants.FILE_EXTENSION_KEY), (String) gridFile
						.getMetaData().get(Constants.BINARY_ENCODING_KEY));
		this.gridFile = gridFile;
	}

	@Override
	public InputStream getEncodedContentStream() {
		return gridFile.getInputStream();
	}

	@Override
	public long getEncodedSize() {
		return gridFile.getLength();
	}
}
