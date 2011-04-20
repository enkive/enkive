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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.codec.Base64OutputStream;
import org.apache.james.mime4j.codec.QuotedPrintableOutputStream;
import org.apache.james.mime4j.parser.MimeEntityConfig;

public class SinglePartHeaderImpl extends AbstractSinglePartHeader implements
		SinglePartHeader {
	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.message");

	public SinglePartHeaderImpl() {

	}

	@Override
	public void parseHeaders(String partHeaders) {
		parseHeaders(partHeaders, new MimeEntityConfig());
	}

	@Override
	public void parseHeaders(String partHeaders, MimeEntityConfig config) {
		ByteArrayInputStream dataStream = new ByteArrayInputStream(
				partHeaders.getBytes());

		org.apache.james.mime4j.message.Message headers = new org.apache.james.mime4j.message.Message();
		try {
			headers = new org.apache.james.mime4j.message.Message(dataStream,
					config);
		} catch (Exception e) {
			logger.error("Could not parse headers for message", e);
		}

		setContentDisposition(headers.getDispositionType());

		setContentType(headers.getMimeType());
		setFileName(headers.getFilename());
	}

	@Override
	public void pushReconstitutedEmail(Writer output) throws IOException {
		output.write(printSinglePartHeader());
		output.flush();
	}

	public String printSinglePartHeader() throws IOException {

		StringBuilder part = new StringBuilder();
		part.append(getOriginalHeaders());
		part.append(getLineEnding());

		if (getContentTransferEncoding() == null) {
			part.append(new String(getContentData().getByteContent()));
			logger.warn("Reconstituting attachment without content transfer encoding specified.  Using Binary Data.");
		} else if (getContentTransferEncoding().equals(
				MimeTransferEncoding.BASE64)) {

			if (getContentData().getMetaDataField("lineLength") != null)
				setMaxLineLength(Integer.parseInt(getContentData()
						.getMetaDataField("lineLength")));

			ByteArrayOutputStream content = new ByteArrayOutputStream();

			Base64OutputStream out = new Base64OutputStream(content,
					getMaxLineLength(), getLineEnding().getBytes());
			out.write(getContentData().getByteContent());
			out.flush();
			out.close();

			part.append(content.toString());

		} else if (getContentTransferEncoding().equals(
				MimeTransferEncoding.SEVEN_BIT)) {
			part.append(new String(getContentData().getByteContent(),
					"US-ASCII"));
		} else if (getContentTransferEncoding().equals(
				MimeTransferEncoding.EIGHT_BIT)) {
			part.append(new String(getContentData().getByteContent(), "UTF-8"));

		} else if (getContentTransferEncoding().equals(
				MimeTransferEncoding.QUOTED_PRINTABLE)) {
			if (!getContentData().getEncodedContentData().isEmpty()) {
				part.append(new String(getContentData().getEncodedContentData()
						.getByteContent()));
			} else {
				ByteArrayOutputStream content = new ByteArrayOutputStream();
				QuotedPrintableOutputStream out = new QuotedPrintableOutputStream(
						content, false);
				out.write(getContentData().getByteContent(), 0,
						getContentData().getByteContent().length);
				out.flush();
				out.close();
				part.append(content.toString());
			}
		} else {
			part.append(new String(getContentData().getByteContent()));
			logger.warn("Reconstituting attachment without content transfer encoding."
					+ " Transfer encoding specified is unsupported."
					+ getContentTransferEncoding().toString());
		}
		part.append(getLineEnding());
		return part.toString();
	}

	@Override
	public Set<String> getAttachmentFileNames() {
		Set<String> result = newSet();
		if (getFilename() != null) {
			result.add(getFilename());
		}
		return result;
	}

	@Override
	public Set<String> getAttachmentTypes() {
		Set<String> result = newSet();
		if (getContentType() != null) {
			result.add(getContentType());
		}
		return result;
	}
}
