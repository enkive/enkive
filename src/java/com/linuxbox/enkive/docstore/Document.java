/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
package com.linuxbox.enkive.docstore;

import java.io.InputStream;

import com.linuxbox.enkive.docstore.exception.DocStoreException;

public interface Document {
	/**
	 * If known will return the size of the document. If not known will return a
	 * negative value. This value should not be trusted to be exact; it's useful
	 * for estimation purposes only.
	 * 
	 * @return
	 */
	long getEncodedSize();

	String getMimeType();

	String getFilename();

	String getFileExtension();

	String getBinaryEncoding();

	/**
	 * Provides an input stream that will produce the encoded content byte by
	 * byte.
	 * 
	 * @return encoded content of document as an input stream
	 * @throws DocStoreException
	 */
	InputStream getEncodedContentStream() throws DocStoreException;

	/**
	 * Provides an input stream that will produce the decoded content byte by
	 * byte.
	 * 
	 * @return decoded content of document as an input stream
	 * @throws DocStoreException
	 */
	InputStream getDecodedContentStream() throws DocStoreException;
}
