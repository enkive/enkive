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
package com.linuxbox.enkive.retriever.mongodb;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.BOUNDARY_ID;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CC;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CONTENT_DISPOSITION;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CONTENT_ID;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CONTENT_TRANSFER_ENCODING;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CONTENT_TYPE;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.DATE;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.EPILOGUE;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.FILENAME;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MAIL_FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MESSAGE_DIFF;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MESSAGE_ID;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.ORIGINAL_HEADERS;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.PREAMBLE;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.RCPT_TO;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.SUBJECT;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.TO;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.ATTACHMENT_ID;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.ATTACHMENT_ID_LIST;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.CONTENT_HEADER;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.CONTENT_HEADER_TYPE;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.MULTIPART_HEADER_TYPE;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.PART_HEADERS;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.SINGLE_PART_HEADER_TYPE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.exception.BadMessageException;
import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.ContentHeader;
import com.linuxbox.enkive.message.EncodedContentReadData;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageImpl;
import com.linuxbox.enkive.message.MessageSummary;
import com.linuxbox.enkive.message.MessageSummaryImpl;
import com.linuxbox.enkive.message.MultiPartHeader;
import com.linuxbox.enkive.message.MultiPartHeaderImpl;
import com.linuxbox.enkive.message.SinglePartHeader;
import com.linuxbox.enkive.message.SinglePartHeaderImpl;
import com.linuxbox.enkive.message.docstore.DocumentEncodedContentReadData;
import com.linuxbox.enkive.retriever.AbstractRetrieverService;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoRetrieverService extends AbstractRetrieverService {

	protected Mongo m = null;
	protected DB messageDb;
	protected DBCollection messageColl;
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.retriever");

	public MongoRetrieverService(Mongo m, String dbName, String collName) {
		this.m = m;
		messageDb = m.getDB(dbName);
		messageColl = messageDb.getCollection(collName);
	}

	@Override
	public Message retrieve(String messageUUID) throws CannotRetrieveException {
		try {
			DBObject messageObject = messageColl.findOne(messageUUID);
			Message message = new MessageImpl();
			message.setId(messageUUID);
			setMessageProperties(message, messageObject);
			message.setContentHeader(makeContentHeader(messageObject));
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Message " + messageUUID + " retrieved");

			return message;
		} catch (IOException e) {
			if (LOGGER.isWarnEnabled())
				LOGGER.warn("Error retrieving message with UUID " + messageUUID);
			throw new CannotRetrieveException(e);
		} catch (BadMessageException e) {
			if (LOGGER.isWarnEnabled())
				LOGGER.warn("Error retrieving message with UUID " + messageUUID);
			throw new CannotRetrieveException(e);
		} catch (Exception e) {
			if (LOGGER.isWarnEnabled())
				LOGGER.warn("Error retrieving message with UUID " + messageUUID);
			throw new CannotRetrieveException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> retrieveAttachmentIds(String messageUUID) {
		DBObject messageObject = messageColl.findOne(messageUUID);
		return (List<String>) messageObject.get(ATTACHMENT_ID_LIST);
	}

	@SuppressWarnings("unchecked")
	@Override
	public MessageSummary retrieveSummary(String messageUUID) {
		DBObject messageObject = messageColl.findOne(messageUUID);
		final MessageSummary result = new MessageSummaryImpl();
		result.setId(messageUUID);

		result.setMessageId((String) messageObject.get(MESSAGE_ID));

		final Date date = (Date) messageObject.get(DATE);
		result.setDate(date);

		result.setSubject((String) messageObject.get(SUBJECT));

		result.setMailFrom((String) messageObject.get(MAIL_FROM));

		result.setRcptTo((List<String>) messageObject.get(RCPT_TO));

		result.setFrom((List<String>) messageObject.get(FROM));

		result.setTo((List<String>) messageObject.get(TO));

		result.setCc((List<String>) messageObject.get(CC));

		result.setOriginalHeaders((String) messageObject.get(ORIGINAL_HEADERS));

		return result;
	}

	private ContentHeader makeContentHeader(DBObject messageObject)
			throws CannotRetrieveException, IOException {
		ContentHeader result = null;
		DBObject contentHeaderObject = (DBObject) messageObject
				.get(CONTENT_HEADER);
		result = makeContentHeaderHelper(contentHeaderObject);
		return result;
	}

	private ContentHeader makeContentHeaderHelper(DBObject contentHeaderObject)
			throws CannotRetrieveException, IOException {
		ContentHeader result = null;

		String headerTypeName = (String) contentHeaderObject
				.get(CONTENT_HEADER_TYPE);
		if (headerTypeName.equals(SINGLE_PART_HEADER_TYPE)) {
			try {
				result = buildContentHeader(contentHeaderObject);
			} catch (DocStoreException e) {
				throw new CannotRetrieveException(
						"Could not retrieve message attachment");
			}
		} else if (headerTypeName.equals(MULTIPART_HEADER_TYPE)) {
			result = buildMultiPartHeader(contentHeaderObject);
		} else {
			throw new CannotRetrieveException(
					"expecting a content_header or multipart_header, but got "
							+ headerTypeName + " instead");
		}

		return result;
	}

	private SinglePartHeader buildContentHeader(DBObject contentHeaderObject)
			throws CannotRetrieveException, IOException, DocStoreException {
		SinglePartHeader header = new SinglePartHeaderImpl();
		setSinglePartHeaderProperties(header, contentHeaderObject);
		final String documentUuid = (String) contentHeaderObject
				.get(ATTACHMENT_ID);

		EncodedContentReadData encodedContentData = null;
		encodedContentData = buildEncodedContentData(documentUuid);

		header.setEncodedContentData(encodedContentData);

		return header;
	}

	@SuppressWarnings("unchecked")
	private MultiPartHeader buildMultiPartHeader(DBObject contentHeaderObject)
			throws CannotRetrieveException, IOException {
		MultiPartHeader multiPartHeader = new MultiPartHeaderImpl();
		setMultiPartHeaderProperties(multiPartHeader, contentHeaderObject);

		ArrayList<BasicDBObject> partHeadersList = (ArrayList<BasicDBObject>) contentHeaderObject
				.get(PART_HEADERS);
		for (BasicDBObject partHeaderObject : partHeadersList) {
			ContentHeader partHeader = makeContentHeaderHelper(partHeaderObject);
			multiPartHeader.addPartHeader(partHeader);
		}

		return multiPartHeader;
	}

	private EncodedContentReadData buildEncodedContentData(String attachmentUUID)
			throws CannotRetrieveException, DocStoreException {
		Document document = docStoreService.retrieve(attachmentUUID);
		DocumentEncodedContentReadData encodedContentData = new DocumentEncodedContentReadData(
				attachmentUUID, document);
		return encodedContentData;
	}

	private void setMessageProperties(Message message, DBObject messageObject)
			throws IOException, BadMessageException {
		if (messageObject.get(ORIGINAL_HEADERS) != null) {
			message.setOriginalHeaders((String) messageObject
					.get(ORIGINAL_HEADERS));
		}

		if (messageObject.get(MESSAGE_DIFF) != null) {
			message.setMessageDiff((String) messageObject.get(MESSAGE_DIFF));
		}
	}

	private void setSinglePartHeaderProperties(SinglePartHeader header,
			DBObject headerObject) throws IOException {
		header.setOriginalHeaders((String) headerObject.get(ORIGINAL_HEADERS));

		header.setContentDisposition((String) headerObject
				.get(CONTENT_DISPOSITION));
		header.setContentTransferEncoding((String) headerObject
				.get(CONTENT_TRANSFER_ENCODING));
		header.setContentType((String) headerObject.get(CONTENT_TYPE));
		header.setContentID((String) headerObject.get(CONTENT_ID));
		header.setFileName((String) headerObject.get(FILENAME));
	}

	private void setMultiPartHeaderProperties(MultiPartHeader header,
			DBObject headerObject) throws IOException {
		header.setOriginalHeaders((String) headerObject.get(ORIGINAL_HEADERS));
		header.setBoundary((String) headerObject.get(BOUNDARY_ID));
		header.setPreamble((String) headerObject.get(PREAMBLE));
		header.setEpilogue((String) headerObject.get(EPILOGUE));
	}

	@Override
	public EncodedContentReadData retrieveAttachment(String attachmentUUID)
			throws CannotRetrieveException {

		EncodedContentReadData attachment;
		try {
			attachment = buildEncodedContentData(attachmentUUID);
		} catch (DocStoreException e) {
			throw new CannotRetrieveException("Could not retrieve attachment",
					e);
		}

		return attachment;
	}

}
