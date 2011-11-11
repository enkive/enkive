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

import java.io.IOException;
import java.io.Writer;

import org.apache.james.mime4j.message.Header;

import com.linuxbox.enkive.exception.BadMessageException;

public interface Message extends MessageSummary {
	/**
	 * 
	 * @return All the headers starting just after the DATA (S|L)MPT command up
	 *         to the blank line that separates the headers from the message
	 *         content. Having the exact headers will allow us to re-constitute
	 *         the original message byte for byte.
	 */
	public String getOriginalHeaders();

	/**
	 * 
	 * @return A string containing the MIME version as determined by the
	 *         headers. Likely will be "1.0" for the foreseeable future. Will
	 *         return null if this is note MIME content.
	 */
	public String getMimeVersion();

	/**
	 * 
	 * @return A string containing the content-type header.
	 */
	public String getContentType();

	/**
	 * 
	 * @return A string containing the content-transfer-encoding header.
	 */
	public String getContentTransferEncoding();

	/**
	 * 
	 * @return the text ContentHeader that is extracted from the message's
	 *         header
	 */
	public ContentHeader getContentHeader();

	/**
	 * Sets the contentHeader attribute to the ContentHeader provided.
	 * 
	 * @param contentHeader
	 */
	public void setContentHeader(ContentHeader contentHeader);

	/**
	 * @param originalHeaders
	 *            A string containing the original headers. It is assumed that
	 *            these headers will be parsed at some point allowing methods
	 *            like getTo() and getSubject() to return information from the
	 *            original headers.
	 * @throws IOException
	 * @throws BadMessageException
	 */
	public void setOriginalHeaders(String originalHeaders)
			throws BadMessageException, IOException;

	/**
	 * 
	 * Sets the mimeVersion attribute
	 * 
	 * @param mimeVersion
	 */
	public void setMimeVersion(String mimeVersion);

	/**
	 * 
	 * Sets the contentType attribute
	 * 
	 * @param contentType
	 */
	public void setContentType(String contentType);

	/**
	 * 
	 * Sets the contentTransferEncoding attribute
	 * 
	 * @param contentTranserEncoding
	 */
	public void setContentTransferEncoding(String contentTransferEncoding);

	/**
	 * 
	 * Sets the parsedHeader attribute
	 * 
	 * @param parsedHeader
	 */
	public void setParsedHeader(Header parsedHeader);

	/**
	 * 
	 * @return mime4j Header Object
	 */
	public Header getParsedHeader();

	public String getMessageDiff();

	public void setMessageDiff(String messageDiff);

	/**
	 * Returns the reconstituted email message as a String.
	 */
	public String getReconstitutedEmail() throws IOException;

	/**
	 * Given a Writer, will push the entire re-constituted email to the stream.
	 * 
	 * TODO SMTP generally uses a 7-bit or 8-bit character set; is a Writer
	 * sufficient, or should it be an OutputStream? Are we supporting SMTP or
	 * ESMTP w/ 8BITMIME?
	 * 
	 * @see http://en.wikipedia.org/wiki/8BITMIME#8BITMIME
	 * 
	 * @param output
	 *            The Writer to push the re-constituted email to.
	 * @throws IOException
	 */
	public void pushReconstitutedEmail(Writer output) throws IOException;

}