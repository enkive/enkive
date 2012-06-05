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
package com.linuxbox.enkive.message.docstore;

import java.io.InputStream;

import com.linuxbox.enkive.docstore.AbstractDocument;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.message.EncodedContentData;

/**
 * This class acts as a bridge between the ContentData data type used by the
 * archiver and the more generic Document type used by out back-end, document
 * storage service.
 * 
 * @author ivancich
 * 
 */
public class ContentDataDocument extends AbstractDocument {
	private EncodedContentData contentData;

	public ContentDataDocument(EncodedContentData contentData, String mimeType,
			String filename, String fileSuffix, String binaryEncoding) {
		super(mimeType, filename, fileSuffix, binaryEncoding);
		this.contentData = contentData;
	}

	/**
	 * Do not know the size, so return negative value.
	 */
	@Override
	public long getEncodedSize() {
		return -1;
	}

	@Override
	public InputStream getEncodedContentStream() {
		return contentData.getBinaryContent();
	}

	@Override
	public InputStream getDecodedContentStream() throws DocStoreException {
		throw new DocStoreException(
				"decoded version of ContentData not available");
	}
}
