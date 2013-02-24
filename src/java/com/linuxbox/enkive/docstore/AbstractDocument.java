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
package com.linuxbox.enkive.docstore;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.apache.james.mime4j.util.MimeUtil;

import com.linuxbox.enkive.docstore.exception.DocStoreException;

/**
 * Provides most of the implementation for a Document. All the subclass really
 * needs to do is supply getEncodedContentStream.
 * 
 * @author ivancich
 * 
 */
public abstract class AbstractDocument implements Document {
	@SuppressWarnings("unused")
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.docstore");

	protected String mimeType;
	protected String fileExtension;
	protected String binaryEncoding;
	protected String filename;

	public AbstractDocument(String mimeType, String filename,
			String fileExtension, String binaryEncoding) {
		this.mimeType = mimeType;
		this.fileExtension = fileExtension;
		this.binaryEncoding = binaryEncoding;
		this.filename = filename;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public String getFileExtension() {
		return fileExtension;
	}

	@Override
	public String getBinaryEncoding() {
		return binaryEncoding;
	}

	@Override
	public InputStream getDecodedContentStream() throws DocStoreException {
		if (MimeUtil.isBase64Encoding(binaryEncoding)) {
			return new Base64InputStream(getEncodedContentStream());
		} else if (MimeUtil.isQuotedPrintableEncoded(binaryEncoding)) {
			return new QuotedPrintableInputStream(getEncodedContentStream());
		} else {
			return getEncodedContentStream();
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
