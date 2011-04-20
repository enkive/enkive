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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.apache.james.mime4j.util.MimeUtil;

import com.linuxbox.enkive.exception.CannotTransferMessageContentException;

public class ContentDataImpl extends AbstractContentData {

	private EncodedContentData encodedContentData;

	public ContentDataImpl() {
		super();
		encodedContentData = new EncodedContentDataImpl();
	}

	public void decodeAndSetContent(InputStream contentStream,
			String transferEncoding)
			throws CannotTransferMessageContentException, IOException {
		if (MimeUtil.isBase64Encoding(transferEncoding)) {
			BufferedReader dataReader = new BufferedReader(
					new InputStreamReader(contentStream));
			dataReader.mark(78);

			try {
				int lineLength = dataReader.readLine().length();
				setMetaDataField("lineLength", Integer.toString(lineLength));
			} catch (Exception e) {
				// Do nothing
			}
			dataReader.reset();
			ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
			int byteRead;
			while ((byteRead = dataReader.read()) != -1)
				tempStream.write(byteRead);

			contentStream = new Base64InputStream(new ByteArrayInputStream(
					tempStream.toByteArray()));

		} else if (MimeUtil.isQuotedPrintableEncoded(transferEncoding)) {
			encodedContentData.setBinaryContent(contentStream);
			contentStream = new QuotedPrintableInputStream(
					encodedContentData.getBinaryContent());
		}
		setBinaryContent(contentStream);
	}

	public EncodedContentData getEncodedContentData() {
		return encodedContentData;
	}

	public void setEncodedContentData(EncodedContentData encodedContentData) {
		this.encodedContentData = encodedContentData;
	}

}
