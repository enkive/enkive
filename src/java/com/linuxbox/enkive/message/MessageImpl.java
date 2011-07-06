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
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.field.address.Address;
import org.apache.james.mime4j.parser.MimeEntityConfig;
import org.apache.james.mime4j.parser.MimeTokenStream;

import com.linuxbox.enkive.exception.BadMessageException;
import com.linuxbox.enkive.exception.CannotTransferMessageContentException;
import com.linuxbox.util.StringUtils;

/**
 * @author lee
 * 
 */
public class MessageImpl extends AbstractMessage implements Message {
	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.message");

	private MimeEntityConfig config;

	public MessageImpl() {
		super();
		config = new MimeEntityConfig();
		config.setStrictParsing(false);
		config.setMaxLineLen(-1);
		config.setMaxContentLen(-1);
	}

	public MessageImpl(InputStream dataStream)
			throws CannotTransferMessageContentException, IOException,
			MimeException, BadMessageException {
		this(StringUtils.stringFromInputStream(dataStream));
	}

	public MessageImpl(String in) throws CannotTransferMessageContentException,
			IOException, MimeException, BadMessageException {
		this();
		InputStream dataStream = new ByteArrayInputStream(in.getBytes());
		ConstructMessage(dataStream);
		calculateMessageDiff(in);
	}

	/**
	 * @throws IOException
	 * @throws MimeIOException
	 * @throws MimeException
	 * @throws CannotTransferMessageContentException
	 * 
	 * @param InputStream
	 *            in An InputStream of the message to be parsed
	 * 
	 *            Constructs a com.linuxbox.enkive.message object from a raw
	 *            email message InputStream
	 * @throws BadMessageException
	 */
	public void ConstructMessage(InputStream in) throws IOException,
			CannotTransferMessageContentException, BadMessageException {

		Stack<MultiPartHeader> headerStack = new Stack<MultiPartHeader>();
		MultiPartHeader mp;
		StringBuilder headers = new StringBuilder();
		boolean messageHeadersParsed = false;
		boolean isMultiPart = false;

		// TODO Get line ending from message
		String lineEnding = "\r\n";

		final MessageStreamParser stream = new MessageStreamParser(config);
		stream.setRecursionMode(MimeTokenStream.M_NO_RECURSE);

		stream.parse(in);

		try {
			for (int state = stream.getState(); state != MimeTokenStream.T_END_OF_STREAM; state = stream
					.next()) {
				switch (state) {

				// At the start of a header section we want to reset the local
				// header variable since we only want to store the headers
				// for the section currently being parsed
				case MimeTokenStream.T_START_HEADER:
					headers = new StringBuilder();
					break;

				// Append each header field to the local header variable
				case MimeTokenStream.T_FIELD:
					headers.append(stream.getField());
					headers.append(lineEnding);
					break;

				// If we haven't set the message headers set them and
				// clear the variable so they don't get stored in a
				// ContentHeader object
				case MimeTokenStream.T_END_HEADER:
					if (!messageHeadersParsed) {
						setOriginalHeaders(headers.toString());
						messageHeadersParsed = true;
						headers = new StringBuilder();
					}
					break;

				// If we have a multipart message, create a new object,
				// grab the information we need and push it on the stack
				case MimeTokenStream.T_START_MULTIPART:
					isMultiPart = true;
					mp = new MultiPartHeaderImpl();
					mp.setBoundary(stream.getBodyDescriptor().getBoundary());
					mp.setOriginalHeaders(headers.toString());
					mp.setLineEnding(lineEnding);
					headerStack.push(mp);
					break;

				// If there's a preamble, get the multipartheader off
				// the top of the stack, set it, and push back on the stack
				case MimeTokenStream.T_PREAMBLE:
					BufferedReader reader = new BufferedReader(
							stream.getReader());

					String tempString;
					String preamble = "";
					while ((tempString = reader.readLine()) != null) {
						preamble += tempString + lineEnding;
					}
					mp = headerStack.pop();
					mp.setPreamble(preamble);
					headerStack.push(mp);
					break;

				// If there's an epilogue, get the multipartheader off
				// the top of the stack, set it, and push back on the stack
				case MimeTokenStream.T_EPILOGUE:
					BufferedReader epilogueReader = new BufferedReader(
							stream.getReader());

					String tempEpilogueString;
					String epilogue = "";
					while ((tempEpilogueString = epilogueReader.readLine()) != null) {
						epilogue += tempEpilogueString + lineEnding;
					}
					mp = headerStack.pop();
					mp.setEpilogue(epilogue);
					headerStack.push(mp);
					break;

				// Create a new singlepartheader, set the headers,
				// set the content_data
				case MimeTokenStream.T_BODY:
					SinglePartHeader single = new SinglePartHeaderImpl();

					EncodedContentDataImpl cd = new EncodedContentDataImpl();
					cd.setBinaryContent(stream.getInputStream());
					single.setContentTransferEncoding(stream
							.getBodyDescriptor().getTransferEncoding());

					single.setOriginalHeaders(headers.toString());
					single.parseHeaders(headers.toString(), config);
					single.setEncodedContentData(cd);
					single.setLineEnding(lineEnding);
					// If we're working with a multipart message,
					// pop, add the singlepartheader, and push.
					// Otherwise just set the singlepartheader
					if (isMultiPart) {
						mp = headerStack.pop();
						mp.addPartHeader(single);
						headerStack.push(mp);
					} else
						this.setContentHeader(single);
					break;

				// If we've reached the end of a multipart, it could
				// be a nested multipart. In that case we'll need to
				// Add the nested multipart to the multipart a level above.
				// If not nested, we've reached the end of the content headers
				// so set it.
				case MimeTokenStream.T_END_MULTIPART:
					mp = headerStack.pop();
					if (headerStack.isEmpty())
						this.setContentHeader(mp);
					else {
						MultiPartHeader mp2 = headerStack.pop();
						mp2.addPartHeader(mp);
						headerStack.push(mp2);
					}
					break;

				}

			}
		} catch (MimeException e) {
			throw new BadMessageException(e);
		} catch (MimeIOException e) {
			throw new BadMessageException(e);
		}
		logger.trace("Message " + this.messageId + "Successfully Parsed");
	}

	@Override
	protected void parseHeaders(String originalHeaders)
			throws BadMessageException, IOException {
		// Convert the string headers to a mime4j message object
		// This way we can take advantage of mime4j's powerful library of
		// functions

		InputStream dataStream = new ByteArrayInputStream(
				originalHeaders.getBytes());
		parseHeaders(dataStream);
	}

	/*
	 * If an InputStream comes in, parse it. I (Eric) refactored this out in
	 * case we ever have an InputStream directly such as when retrieving to
	 * possibly avoid converting from an InputStream to a String to an
	 * InputStream. But it may not be helpful as we'll need the String anyway.
	 */
	protected void parseHeaders(InputStream dataStream)
			throws BadMessageException, IOException {
		org.apache.james.mime4j.message.Message headers = new org.apache.james.mime4j.message.Message();

		try {
			headers = new org.apache.james.mime4j.message.Message(dataStream,
					config);
		} catch (MimeIOException e) {
			throw new BadMessageException(e);
		}

		if (headers.getFrom() != null) {
			setFrom(headers.getFrom().toString());
		}

		if (headers.getTo() != null)
			for (Address to : headers.getTo()) {
				appendTo(to.toString());
			}
		
		if (headers.getCc() != null){
			for (Address cc : headers.getCc()) {
				appendCc(cc.toString());
			}
		}		
		if (headers.getSubject() != null)
			setSubject(headers.getSubject());
		if (headers.getMessageId() != null)
			setMessageId(headers.getMessageId());

		// Check for date, if there is none, get the current time
		if (headers.getDate() != null)
			setDate(headers.getDate());
		else
			setDate(new Date());

		if (headers.getMimeType() != null)
			setContentType(headers.getMimeType());

		if (headers.getContentTransferEncoding() != null)
			setContentTransferEncoding(headers.getContentTransferEncoding());

		if (headers.getHeader().getField("MIME-VERSION") != null)
			setMimeVersion(headers.getHeader().getField("MIME-VERSION")
					.getBody().toString());

		setParsedHeader(headers.getHeader());
	}

	private void calculateMessageDiff(String originalMessage) throws IOException {
		String patchText = differ.patch_toText(differ.patch_make(differ.diff_main(
				getUnpatchedEmail(), originalMessage)));
		setMessageDiff(patchText);
	}
}
