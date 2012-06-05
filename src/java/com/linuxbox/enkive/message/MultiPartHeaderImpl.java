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

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.stream.MimeConfig;

public class MultiPartHeaderImpl extends AbstractMultiPartHeader implements
		MultiPartHeader {
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.message");

	@Override
	public void parseHeaders(String someContentHeader) {
		if (LOGGER.isErrorEnabled())
			LOGGER.error("unimplemented MultiPartHeaderImpl:parseHeaders is being called");
	}

	@Override
	public void parseHeaders(String partHeader, MimeConfig config) {
		parseHeaders(partHeader);
	}

	@Override
	public void pushReconstitutedEmail(Writer output) throws IOException {
		output.write(printMultiPartHeader());
		output.flush();
	}

	public String printMultiPartHeader() throws IOException {
		String msg = getOriginalHeaders() + getLineEnding() + getPreamble()
				+ getLineEnding();
		List<ContentHeader> partHeaders = getPartHeaders();
		StringBuffer boundary = new StringBuffer(getBoundary()
				+ getLineEnding());
		for (int i = 0; i < partHeaders.size(); i++) {
			msg += "--" + boundary.toString();
			if (partHeaders.get(i).isMultipart())
				msg += ((MultiPartHeader) partHeaders.get(i))
						.printMultiPartHeader() + getLineEnding();
			else
				msg += ((SinglePartHeader) partHeaders.get(i))
						.printSinglePartHeader();
		}
		msg += "--"
				+ boundary.insert(boundary.toString().trim().length(), "--"
						+ getLineEnding());

		msg += getEpilogue() + getLineEnding();

		return msg;
	}

	@Override
	public Set<String> getAttachmentFileNames() {
		Set<String> result = newSet();
		for (ContentHeader header : getPartHeaders()) {
			result.addAll(header.getAttachmentFileNames());
		}
		return result;
	}

	@Override
	public Set<String> getAttachmentTypes() {
		Set<String> result = newSet();
		for (ContentHeader header : getPartHeaders()) {
			result.addAll(header.getAttachmentTypes());
		}
		return result;
	}

	@Override
	public Set<String> getAttachmentUUIDs() {

		Set<String> result = newSet();
		for (ContentHeader header : getPartHeaders()) {
			result.addAll(header.getAttachmentUUIDs());
		}
		return result;
	}
}
