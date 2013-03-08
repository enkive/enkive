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
package com.linuxbox.enkive.archiver.mongodb;

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
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MIME_VERSION;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.ORIGINAL_HEADERS;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.PREAMBLE;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.RCPT_TO;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.SUBJECT;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.TO;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.ARCHIVE_TIME;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.ATTACHMENT_ID;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.ATTACHMENT_ID_INDEX;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.ATTACHMENT_ID_LIST;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.CONTENT_HEADER;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.CONTENT_HEADER_TYPE;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.MESSAGE_UUID;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.MULTIPART_HEADER_TYPE;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.NESTED_MESSAGE_ID_INDEX;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.NESTED_MESSAGE_ID_LIST;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.PART_HEADERS;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.SINGLE_PART_HEADER_TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.util.MimeUtil;

import com.linuxbox.enkive.archiver.AbstractMessageArchivingService;
import com.linuxbox.enkive.archiver.MessageLoggingText;
import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.archiver.exceptions.FailedToEmergencySaveException;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.docstore.StoreRequestResult;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.exception.BadMessageException;
import com.linuxbox.enkive.exception.CannotTransferMessageContentException;
import com.linuxbox.enkive.message.ContentException;
import com.linuxbox.enkive.message.ContentHeader;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageImpl;
import com.linuxbox.enkive.message.MimeTransferEncoding;
import com.linuxbox.enkive.message.MultiPartHeader;
import com.linuxbox.enkive.message.SinglePartHeader;
import com.linuxbox.enkive.message.docstore.ContentDataDocument;
import com.linuxbox.util.mongodb.MongoIndexable;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

public class MongoArchivingService extends AbstractMessageArchivingService
		implements MongoIndexable {

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.archiveService.mongodb");

	protected Mongo m = null;
	protected DB messageDb;
	protected DBCollection messageColl;
	protected List<String> attachment_ids;
	protected List<String> nested_message_ids;

	public MongoArchivingService(Mongo m, String dbName, String collName) {
		this.m = m;
		messageDb = m.getDB(dbName);
		messageColl = messageDb.getCollection(collName);
	}

	@Override
	public String storeMessage(Message message) throws CannotArchiveException,
			FailedToEmergencySaveException, AuditServiceException {
		String messageUUID = null;
		attachment_ids = new ArrayList<String>();
		nested_message_ids = new ArrayList<String>();

		String messageId = calculateMessageId(message);

		try {
			BasicDBObject messageObject = new BasicDBObject();
			messageObject.put(MESSAGE_UUID, messageId);
			messageObject.put(ARCHIVE_TIME, new Date());
			messageObject.put(ORIGINAL_HEADERS, message.getOriginalHeaders());
			messageObject.put(MAIL_FROM, message.getMailFrom());
			messageObject.put(RCPT_TO, message.getRcptTo());
			messageObject.put(FROM, message.getFrom());
			messageObject.put(TO, message.getTo());
			messageObject.put(CC, message.getCc());
			messageObject.put(DATE, message.getDate());
			messageObject.put(SUBJECT, message.getSubject());
			messageObject.put(MESSAGE_ID, message.getMessageId());
			messageObject.put(MIME_VERSION, message.getMimeVersion());
			messageObject.put(CONTENT_TYPE, message.getContentType());
			messageObject.put(MESSAGE_DIFF, message.getMessageDiff());
			ContentHeader contentHeader = message.getContentHeader();
			if (message.getContentType().trim().toLowerCase()
					.equals(ContentTypeField.TYPE_MESSAGE_RFC822.toLowerCase())) {
				String subMessageUUID = storeNestedMessage(message
						.getContentHeader().getEncodedContentData()
						.getEncodedContent(),
						message.getContentTransferEncoding());
				if (!nested_message_ids.contains(subMessageUUID))
					nested_message_ids.add(subMessageUUID);
			}
			messageObject.put(CONTENT_HEADER,
					archiveContentHeader(contentHeader));
			messageObject.put(ATTACHMENT_ID_LIST, attachment_ids);
			messageObject.put(NESTED_MESSAGE_ID_LIST, nested_message_ids);
			messageColl.insert(messageObject);
			messageUUID = messageObject.getString(MESSAGE_UUID);
		} catch (MongoException e) {
			throw new CannotArchiveException(e);
		} catch (DocStoreException e) {
			throw new CannotArchiveException(e);
		} catch (ContentException e) {
			throw new CannotArchiveException(e);
		}
		if (LOGGER.isInfoEnabled())
			LOGGER.info(MessageLoggingText.MESSAGE_STORED_TEXT + messageUUID);
		return messageUUID;
	}

	@Override
	public String findMessage(Message message) throws MongoException,
			CannotArchiveException {
		String messageUUID = null;
		DBObject messageObject = messageColl
				.findOne(calculateMessageId(message));
		if (messageObject != null) {
			messageUUID = (String) messageObject.get(MESSAGE_UUID);
			if (LOGGER.isInfoEnabled())
				LOGGER.info(MessageLoggingText.DUPLICATE_FOUND_TEXT
						+ messageUUID);
		}
		return messageUUID;
	}

	// FIXME: this seems like it's not Mongo dependent (well, except that it's
	// returning a MongoDBObject), but instead dependent on
	// the doc store service, so couldn't this be moved up to the
	// AbstractMessageArchivingService class?
	private BasicDBObject archiveContentHeader(ContentHeader contentHeader)
			throws DocStoreException, CannotArchiveException, ContentException,
			FailedToEmergencySaveException, AuditServiceException {
		BasicDBObject headerObject = new BasicDBObject();
		if (contentHeader.isMultipart()) {
			MultiPartHeader multiPartHeader = (MultiPartHeader) contentHeader;
			headerObject.put(CONTENT_HEADER_TYPE, MULTIPART_HEADER_TYPE);
			headerObject.put(BOUNDARY_ID, multiPartHeader.getBoundary());
			headerObject.put(PREAMBLE, multiPartHeader.getPreamble());
			headerObject.put(EPILOGUE, multiPartHeader.getEpilogue());
			headerObject.put(ORIGINAL_HEADERS,
					multiPartHeader.getOriginalHeaders());
			List<ContentHeader> partHeaders = ((MultiPartHeader) contentHeader)
					.getPartHeaders();
			ArrayList<BasicDBObject> partHeadersList = new ArrayList<BasicDBObject>();
			for (ContentHeader partHeader : partHeaders) {
				partHeadersList.add(archiveContentHeader(partHeader));
			}
			headerObject.put(PART_HEADERS, partHeadersList);
		} else {
			final SinglePartHeader singlePartHeader = (SinglePartHeader) contentHeader;

			headerObject.put(CONTENT_HEADER_TYPE, SINGLE_PART_HEADER_TYPE);
			headerObject.put(CONTENT_ID, singlePartHeader.getContentID());
			headerObject.put(CONTENT_TYPE, singlePartHeader.getContentType());
			headerObject.put(CONTENT_DISPOSITION,
					singlePartHeader.getContentDisposition());
			if (singlePartHeader.getFilename() != null)
				headerObject.put(FILENAME, singlePartHeader.getFilename());

			MimeTransferEncoding mtf = singlePartHeader
					.getContentTransferEncoding();
			if (mtf != null) {
				headerObject.put(CONTENT_TRANSFER_ENCODING, mtf.toString());
			}

			headerObject.put(ORIGINAL_HEADERS,
					singlePartHeader.getOriginalHeaders());

			if (singlePartHeader.getContentType().trim().toLowerCase()
					.equals(ContentTypeField.TYPE_MESSAGE_RFC822.toLowerCase())) {
				String subMessageUUID = storeNestedMessage(singlePartHeader
						.getEncodedContentData().getEncodedContent(),
						singlePartHeader.getContentTransferEncoding()
								.toString());
				if (!nested_message_ids.contains(subMessageUUID))
					nested_message_ids.add(subMessageUUID);
			}

			String fileExtension = "";
			if (singlePartHeader.getFilename() != null)
				fileExtension = singlePartHeader.getFilename().substring(
						singlePartHeader.getFilename().lastIndexOf('.') + 1);
			// store the attachment
			ContentDataDocument document = new ContentDataDocument(
					singlePartHeader.getEncodedContentData(),
					singlePartHeader.getContentType(), fileExtension,
					singlePartHeader.getFilename(), singlePartHeader
							.getContentTransferEncoding().toString());
			StoreRequestResult docResult = docStoreService.store(document);

			final String attachmentIdentifier = docResult.getIdentifier();
			headerObject.put(ATTACHMENT_ID, attachmentIdentifier);
			attachment_ids.add(attachmentIdentifier);
		}

		return headerObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeMessage(String messageUUID) {
		if (!messageReferencedAsNestedMessage(messageUUID)) {
			DBObject messageObject = messageColl.findOne(messageUUID);

			// Get list of attachments and handle them
			List<String> attachmentIds = (List<String>) messageObject
					.get(ATTACHMENT_ID_LIST);
			for (String attachmentId : attachmentIds) {
				try {
					if (!attachmentReferencedByMultipleMessages(attachmentId)) {
						docStoreService.removeWithRetries(attachmentId, 1, 0);
					} else
						LOGGER.info("Attachment referenced by more than one message, not removing: "
								+ attachmentId);
				} catch (DocStoreException e) {
					LOGGER.warn("Could not remove attachment: " + attachmentId,
							e);
				}
				// Remove message object
			}
			WriteResult result = messageColl.remove(messageObject);
			LOGGER.info("Message Removed: " + messageUUID);
			return result.getLastError().ok();

		} else {
			LOGGER.info("Message referenced as nested message, not removing: "
					+ messageUUID);
			return false;
		}
	}

	protected String storeNestedMessage(InputStream nestedMessage,
			String contentTransferEncoding) throws CannotArchiveException,
			FailedToEmergencySaveException, AuditServiceException {
		String nestedMessageUUID = "";
		try {
			Message subMessage;
			if (MimeUtil.isBase64Encoding(contentTransferEncoding)) {
				subMessage = new MessageImpl(new Base64InputStream(
						nestedMessage));
			} else if (MimeUtil
					.isQuotedPrintableEncoded(contentTransferEncoding)) {
				subMessage = new MessageImpl(new QuotedPrintableInputStream(
						nestedMessage));
			} else
				subMessage = new MessageImpl(nestedMessage);
			nestedMessageUUID = storeOrFindMessage(subMessage);
		} catch (CannotTransferMessageContentException e) {
			throw new CannotArchiveException(
					"Could not parse embedded message/rfc822", e);
		} catch (BadMessageException e) {
			throw new CannotArchiveException(
					"Could not parse embedded message/rfc822", e);
		} catch (IOException e) {
			throw new CannotArchiveException(
					"Could not parse embedded message/rfc822", e);
		} catch (MimeException e) {
			throw new CannotArchiveException(
					"Could not parse embedded message/rfc822", e);
		}
		return nestedMessageUUID;
	}

	private boolean attachmentReferencedByMultipleMessages(String attachmentId) {
		BasicDBObject attachmentQuery = new BasicDBObject(ATTACHMENT_ID_LIST,
				attachmentId);
		DBCursor results = messageColl.find(attachmentQuery);
		return (results.size() > 1);
	}

	private boolean messageReferencedAsNestedMessage(String messageId) {
		BasicDBObject nestedMessageQuery = new BasicDBObject(
				NESTED_MESSAGE_ID_LIST, messageId);
		DBCursor results = messageColl.find(nestedMessageQuery);
		return (results.size() > 0);
	}

	@Override
	public void subStartup() {
		// Do nothing
	}

	@Override
	public void subShutdown() {
		// Do nothing
	}

	@Override
	public List<DBObject> getIndexInfo() {
		return messageColl.getIndexInfo();
	}

	@Override
	public List<IndexDescription> getPreferredIndexes() {
		List<IndexDescription> result = new LinkedList<IndexDescription>();

		final DBObject whenIndex = new BasicDBObject(ATTACHMENT_ID_LIST, 1);
		IndexDescription id1 = new IndexDescription(ATTACHMENT_ID_INDEX,
				whenIndex, false);
		result.add(id1);

		final DBObject whatWhenIndex = new BasicDBObject(
				NESTED_MESSAGE_ID_LIST, 1);
		IndexDescription id2 = new IndexDescription(NESTED_MESSAGE_ID_INDEX,
				whatWhenIndex, false);
		result.add(id2);

		return result;
	}

	@Override
	public void ensureIndex(DBObject index, DBObject options)
			throws MongoException {
		messageColl.ensureIndex(index, options);
	}

	@Override
	public long getDocumentCount() throws MongoException {
		return messageColl.getCount();
	}
}
