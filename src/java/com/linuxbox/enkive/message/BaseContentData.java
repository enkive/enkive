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
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.linuxbox.enkive.exception.CannotTransferMessageContentException;

public interface BaseContentData extends BaseContentReadData {
	/*
	 * Perhaps we probably only need either getBinaryContent() or
	 * transferBinaryContent() at this point. If they're trivial to implement,
	 * we might as well have both. If it would be better just to implement one,
	 * we can figure out which would be better from the perspective of other,
	 * inter-related APIs.
	 */

	/**
	 * 
	 * @param content
	 *            A byte array that represents entire binary content of the
	 *            attachment/text.
	 */
	public void setByteContent(byte[] content);

	/**
	 * Given an InputStream, reads the binary content and stores it internally.
	 * 
	 * @param contentStream
	 *            an InputStream from which the data can be extracted.
	 */
	public void setBinaryContent(InputStream contentStream)
			throws CannotTransferMessageContentException;

	/**
	 * Given a String, transfers the binary content into the ContentData.
	 * 
	 * @param content
	 *            The string containing the content.
	 * @param encoding
	 */
	public void setBinaryContent(String content, Charset encoding);

	
	/**
	 * 
	 * @return array of 20 bytes (since SHA-1 produces a hash of 160 bits)
	 */
	byte[] getSha1();

	/**
	 * 
	 * @return A byte array that represents entire binary content of the
	 *         attachment/text. Given that MIME-encoded content ultimately is
	 *         sent as text-encoded (e.g., base64), this will be the binary
	 *         version of that.
	 */
	byte[] getByteContent();

	/**
	 * Takes the binary content stored internally and dumps it to the
	 * OutputStream passed in. Is responsible for flushing the output but not
	 * for closing.
	 * 
	 * @param out
	 *            the OutputStream to send the binary content to.
	 */
	void transferBinaryContent(OutputStream out)
			throws CannotTransferMessageContentException;

}
