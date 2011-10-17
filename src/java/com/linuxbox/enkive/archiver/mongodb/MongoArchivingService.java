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
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.ATTACHMENT_ID_LIST;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.CONTENT_HEADER;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.CONTENT_HEADER_TYPE;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.MESSAGE_UUID;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.MULTIPART_HEADER_TYPE;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.PART_HEADERS;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.SINGLE_PART_HEADER_TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.apache.james.mime4j.field.ContentTypeField;
import org.apache.james.mime4j.util.MimeUtil;

import com.linuxbox.enkive.archiver.AbstractMessageArchivingService;
import com.linuxbox.enkive.archiver.MessageLoggingText;
import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.archiver.exceptions.FailedToEmergencySaveException;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.StoreRequestResult;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.exception.BadMessageException;
import com.linuxbox.enkive.exception.CannotTransferMessageContentException;
import com.linuxbox.enkive.message.ContentHeader;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageImpl;
import com.linuxbox.enkive.message.MimeTransferEncoding;
import com.linuxbox.enkive.message.MultiPartHeader;
import com.linuxbox.enkive.message.SinglePartHeader;
import com.linuxbox.enkive.message.docstore.ContentDataDocument;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoArchivingService extends AbstractMessageArchivingService {

	private final static Log logger = LogFactory
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
		try {
			BasicDBObject messageObject = new BasicDBObject();
			messageObject.put(MESSAGE_UUID, calculateMessageId(message));
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
						.getBinaryContent(),
						message.getContentTransferEncoding());
				if (!nested_message_ids.contains(subMessageUUID))
					nested_message_ids.add(subMessageUUID);
			}
			messageObject.put(CONTENT_HEADER,
					archiveContentHeader(contentHeader));
			messageObject.put(ATTACHMENT_ID_LIST, attachment_ids);
			messageObject.put(ATTACHMENT_ID_LIST, nested_message_ids);
			messageColl.insert(messageObject);
			messageUUID = messageObject.getString(MESSAGE_UUID);
		} catch (MongoException e) {
			throw new CannotArchiveException(e);
		} catch (DocStoreException e) {
			throw new CannotArchiveException(e);
		}
		logger.info(MessageLoggingText.MESSAGE_STORED_TEXT + messageUUID);
		return messageUUID;
	}

	@Override
	public String findMessage(Message message) throws MongoException,
			CannotArchiveException {
		String messageUUID = null;
		DBObject messageObject = messageColl
				.findOne(calculateMessageId(message));
		if (messageObject != null) {
			messageUUID = (String) messageObject.get("_id");
			logger.info(MessageLoggingText.DUPLICATE_FOUND_TEXT + messageUUID);
		}
		return messageUUID;
	}

	private BasicDBObject archiveContentHeader(ContentHeader contentHeader)
			throws DocStoreException, CannotArchiveException,
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
			SinglePartHeader singlePartHeader = (SinglePartHeader) contentHeader;
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
						.getEncodedContentData().getBinaryContent(),
						singlePartHeader.getContentTransferEncoding()
								.toString());
				if (!nested_message_ids.contains(subMessageUUID))
					nested_message_ids.add(subMessageUUID);
			}

			String fileExtension = "";
			if (singlePartHeader.getFilename() != null)
				fileExtension = singlePartHeader.getFilename().substring(
						singlePartHeader.getFilename().lastIndexOf('.') + 1);
			// Store the attachment
			Document document = new ContentDataDocument(
					singlePartHeader.getEncodedContentData(),
					singlePartHeader.getContentType(), fileExtension,
					singlePartHeader.getFilename(), singlePartHeader
							.getContentTransferEncoding().toString());
			StoreRequestResult docResult = docStoreService.store(document);
			headerObject.put(ATTACHMENT_ID, docResult.getIdentifier());
			attachment_ids.add(docResult.getIdentifier());
		}

		return headerObject;
	}

	@Override
	public boolean removeMessage(String messageUUID) {
		try {
			DBObject messageObject = messageColl.findOne(messageUUID);
			messageColl.remove(messageObject);
		} catch (MongoException e) {
			return false;
		}
		return true;
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

	@Override
	public void subStartup() {
		// Do nothing
	}

	@Override
	public void subShutdown() {
		// Do nothing
	}
}
