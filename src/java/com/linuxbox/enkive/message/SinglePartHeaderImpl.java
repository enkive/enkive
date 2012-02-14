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


package com.linuxbox.enkive.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.stream.MimeConfig;

public class SinglePartHeaderImpl extends AbstractSinglePartHeader implements
		SinglePartHeader {
	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.message");

	public SinglePartHeaderImpl() {

	}

	@Override
	public void parseHeaders(String partHeaders) {
		parseHeaders(partHeaders, new MimeConfig());
	}

	@Override
	public void parseHeaders(String partHeaders, MimeConfig config) {
		ByteArrayInputStream dataStream = new ByteArrayInputStream(
				partHeaders.getBytes());

		org.apache.james.mime4j.dom.Message headers = new org.apache.james.mime4j.message.MessageImpl();
		try {
			DefaultMessageBuilder builder = new org.apache.james.mime4j.message.DefaultMessageBuilder();
			builder.setMimeEntityConfig(config);
			headers = builder.parseMessage(dataStream);
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
		part.append(new String(getEncodedContentData().getByteContent()));
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

	@Override
	public Set<String> getAttachmentUUIDs() {
		Set<String> result = newSet();

		result.add(getEncodedContentData().getUUID());
		return result;
	}
}
