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
package com.linuxbox.enkive.message;

import java.io.InputStream;

import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.apache.james.mime4j.util.MimeUtil;

public class EncodedContentDataImpl extends AbstractBaseContentData implements
		EncodedContentData {
	
	protected String uuid;
	protected String filename;
	protected String mimeType;
	protected String transferEncoding;

	public EncodedContentDataImpl(String transferEncoding) {
		super();
		this.transferEncoding = transferEncoding;
	}

	public boolean isEmpty() {
		return (data == null);
	}
	
	public void setUUID(String uUID) {
		this.uuid = uUID;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public String getUUID() {
		return filename;
	}

	@Override
	public InputStream getEncodedContent() throws ContentException {
		return super.getBinaryContent();
	}
	
	@Override
	public InputStream getBinaryContent() throws ContentException {
		if (MimeUtil.isBase64Encoding(transferEncoding)) {
			return new Base64InputStream(super.getBinaryContent());
		} else if (MimeUtil.isQuotedPrintableEncoded(transferEncoding)) {
			return new QuotedPrintableInputStream(super.getBinaryContent());
		} else {
			return super.getBinaryContent();
		}
	}
}
