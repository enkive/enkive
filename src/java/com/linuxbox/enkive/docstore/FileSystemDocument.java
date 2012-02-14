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
package com.linuxbox.enkive.docstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.linuxbox.enkive.docstore.exception.DocStoreException;

public class FileSystemDocument extends AbstractDocument {
	private File theFile;

	public FileSystemDocument(String path, String mimeType, String filename,
			String fileExtension, String binaryEncoding) {
		super(mimeType, filename, fileExtension, binaryEncoding);
		theFile = new File(path);
	}

	@Override
	public long getEncodedSize() {
		return theFile.length();
	}

	@Override
	public InputStream getEncodedContentStream() throws DocStoreException {
		try {
			FileInputStream fileStream = new FileInputStream(theFile);
			return fileStream;
		} catch (IOException e) {
			throw new DocStoreException(e);
		}
	}
}
