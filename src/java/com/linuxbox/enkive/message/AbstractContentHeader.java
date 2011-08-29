/*
 *  Copyright 2010 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.enkive.message;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractContentHeader implements ContentHeader {
	protected String originalHeaders;
	protected String contentDisposition;
	protected String contentID;
	protected MimeTransferEncoding contentTransferEncoding;
	protected String contentType;
	protected String fileName;

	protected String lineEnding;
	protected int maxLineLength;

	protected EncodedContentData encodedContentData;

	public AbstractContentHeader() {
		super();

		// initialize this to a default value in case this is a non-MIME encoded
		// email?
		this.contentType = "text/plain";

		// initialize these values in case there is an error in getting the
		// values
		this.lineEnding = "\r\n";
		this.maxLineLength = 76;

	}

	protected Set<String> newSet() {
		return new HashSet<String>();
	}

	@Override
	public void setOriginalHeaders(String originalHeaders) {
		this.originalHeaders = originalHeaders;
	}

	@Override
	public String getOriginalHeaders() {
		return originalHeaders;
	}

	@Override
	public String getContentDisposition() {
		return contentDisposition;
	}

	@Override
	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}

	@Override
	public String getContentID() {
		return contentID;
	}

	@Override
	public void setContentID(String contentID) {
		this.contentID = contentID;
	}

	@Override
	public MimeTransferEncoding getContentTransferEncoding() {
		return contentTransferEncoding;
	}

	@Override
	public void setContentTransferEncoding(String transferEncoding) {
		this.contentTransferEncoding = MimeTransferEncoding
				.parseString(transferEncoding);
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public String getFilename() {
		return fileName;
	}

	@Override
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String getLineEnding() {
		return lineEnding;
	}

	@Override
	public void setLineEnding(String lineEnding) {
		this.lineEnding = lineEnding;
	}

	@Override
	public int getMaxLineLength() {
		return maxLineLength;
	}

	@Override
	public void setMaxLineLength(int maxLineLength) {
		this.maxLineLength = maxLineLength;
	}

	@Override
	public EncodedContentData getEncodedContentData() {
		return encodedContentData;
	}

	@Override
	public void setEncodedContentData(EncodedContentData encodedContentData) {
		this.encodedContentData = encodedContentData;
	}
}