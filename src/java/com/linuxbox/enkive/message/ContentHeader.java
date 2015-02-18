/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
import java.util.Deque;
import java.util.List;
import java.util.Set;

import org.apache.james.mime4j.stream.MimeConfig;

public interface ContentHeader {
	/**
	 * 
	 * @return A string containing the original headers of this part. Often this
	 *         will be empty. However if this part is a component of a
	 *         Multi-part MIME type, this will likely include the Content-Type,
	 *         Content-Transfer-Encoding, etc. This String should contain the
	 *         headers exactly as they appear in the original message, so the
	 *         message can be reconstituted.
	 */
	public String getOriginalHeaders();

	/**
	 * @param originalHeaders
	 *            A string containing the original headers. It is presumed that
	 *            this will be parsed at some point and key information
	 *            extracted, so methods such as getConentID() and
	 *            getContentType() will return information as contained in this
	 *            header.
	 */
	public void setOriginalHeaders(String originalHeaders);

	/**
	 * 
	 * @return A string containing the value of the "Content-Type" header.
	 *         Possible return values include "text/plain" and
	 *         "application/octet-stream".
	 */
	public String getContentType();

	public void setContentType(String contentType);

	/**
	 * 
	 * @return A string containing the value of the "Content-ID" header. For
	 *         example, if the header below appeared, this would return
	 *         "<5.31.32252.1057009685@server01.example.net>".
	 * 
	 *         Content-ID: <5.31.32252.1057009685@server01.example.net>
	 */
	public String getContentID();

	public void setContentID(String contentID);

	/**
	 * 
	 * @return the value of the "Content-Disposition" header.
	 * 
	 *         Content-Disposition: attachment; filename=genome.jpeg;
	 *         modification-date="Wed, 12 Feb 1997 16:29:51 -0500";
	 */
	public String getContentDisposition();

	/**
	 * 
	 * @param the
	 *            value of the "Content-Disposition" header.
	 * 
	 */
	public void setContentDisposition(String contentDisposition);

	/**
	 * 
	 * @return A string containing the filename if a filename appears in the
	 *         Content-Disposition header. Otherwise return null.
	 */
	public String getFilename();

	/**
	 * 
	 * @param fileName
	 *            the file name of the attachment
	 */
	public void setFileName(String fileName);

	/**
	 * 
	 * @return 7bit, base64, quoted-printable (these are in the
	 *         MimeTransferEncoding enum). Do we want to support 8bit and binary
	 *         at this point?
	 */
	public MimeTransferEncoding getContentTransferEncoding();

	/**
	 * 
	 * @param 7bit, base64, quoted-printable
	 */
	public void setContentTransferEncoding(String transferEncoding);

	/**
	 * 
	 * @return the line terminating code for the message Usually "\n" or "\r\n"
	 */
	public String getLineEnding();

	/**
	 * 
	 * @param the
	 *            line terminating code for the message
	 */
	public void setLineEnding(String lineEnding);

	/**
	 * 
	 * @return the max line length for the message Usually 72 or 76
	 */
	public int getMaxLineLength();

	/**
	 * 
	 * @param the
	 *            max line length for the message
	 */
	public void setMaxLineLength(int maxLineLength);

	/**
	 * 
	 * @return a reference to the ContentData
	 */
	public EncodedContentReadData getEncodedContentData();

	/**
	 * 
	 * @param contentData
	 *            the content data reference that holds the content that goes
	 *            with the message or message-part.
	 */
	public void setEncodedContentData(EncodedContentReadData encodedContentData);

	/**
	 * Reconstitutes the sequence of characters in the original message
	 * describing the content header.
	 * 
	 * @param output
	 *            The writer to send the reconstituted header to.
	 * @throws IOException
	 */
	public void pushReconstitutedEmail(Writer output) throws IOException;

	/**
	 * As the header is parsed in the Message class, or aspects of the header
	 * that relate to the content, this will be called, so it can store data in
	 * the appropriate attributes. So for example, when Message sees a header
	 * for "Content-Type", it can call this method, which will in turn set the
	 * internally tracked mimeVersion attribute.
	 * 
	 * @param someContentHeader
	 *            a header that is believed to be a content header.
	 */
	public void parseHeaders(String someContentHeader);

	public void parseHeaders(String string, MimeConfig config);

	/**
	 * 
	 * @return a boolean of whether or not the message is a Multipart Header
	 */
	public boolean isMultipart();

	/**
	 * 
	 * @return a set of all filenames that are attached; filenames include
	 *         extensions.
	 */
	public Set<String> getAttachmentFileNames();

	/**
	 * 
	 * @return a set of all attachment types as MIME types (e.g., "text/plain"
	 *         and "text/html").
	 */
	public Set<String> getAttachmentTypes();

	/**
	 * 
	 * @return a set of all attachment UUIDs .
	 */
	public Set<String> getAttachmentUUIDs();

	/**
	 * 
	 * @return a summary of each attachment at or below this point in the header
	 *         hierarchy
	 */
	public List<AttachmentSummary> getAttachmentSummaries();
	
	/**
	 * Recursively build the list of attachment summaries. Since attachments can
	 * be hierarchical, the positionsAbove is a list of integers indicating
	 * where in the hierarchy a given attachment sits. For example, if it were
	 * the list {2, 1, 3} then it would be the third sub-attachment, of the
	 * first sub-attachment, of the second attachment. Since this is made for
	 * human consumption, the indices are 1-based rather than 0-based.
	 * 
	 * @param result
	 *            the list on which to add results
	 * @param positionsAbove
	 *            the list of positions above, so the hierarchy can be
	 *            represented yet the result be flat
	 */
	public abstract void generateAttachmentSummaries(
			List<AttachmentSummary> result, Deque<Integer> positionsAbove);
}
