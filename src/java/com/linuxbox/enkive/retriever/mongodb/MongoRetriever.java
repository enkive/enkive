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

package com.linuxbox.enkive.retriever.mongodb;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.BOUNDARY_ID;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CC;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CONTENT_DISPOSITION;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CONTENT_ID;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CONTENT_TRANSFER_ENCODING;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CONTENT_TYPE;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.DATE;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.EPILOGUE;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MAIL_FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MESSAGE_ID;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.ORIGINAL_HEADERS;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.PREAMBLE;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.RCPT_TO;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.SUBJECT;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.TO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.exception.BadMessageException;
import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.exception.CannotTransferMessageContentException;
import com.linuxbox.enkive.message.ContentHeader;
import com.linuxbox.enkive.message.EncodedContentData;
import com.linuxbox.enkive.message.EncodedContentDataImpl;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageImpl;
import com.linuxbox.enkive.message.MessageSummary;
import com.linuxbox.enkive.message.MessageSummaryImpl;
import com.linuxbox.enkive.message.MultiPartHeader;
import com.linuxbox.enkive.message.MultiPartHeaderImpl;
import com.linuxbox.enkive.message.SinglePartHeader;
import com.linuxbox.enkive.message.SinglePartHeaderImpl;
import com.linuxbox.enkive.message.Utility;
import com.linuxbox.enkive.retriever.AbstractArchiveService;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;

/**
 * Currently this class needs to be instantiated with enough information for it
 * to create a session that will provide access to the JCR API when a Core API
 * NodeRef come in (specifically the call to session.getNodeByUUID). This might
 * be better written to stick with the Core API.
 * 
 * TODO : will we want to remove the credentials that get passed in when this
 * bean is created along with the code for creating of a session using those
 * credentials? Instead we would strictly use the already existing session.
 * 
 * TODO : Perhaps we need an interface that represents a repository. It could
 * have whatever credentials/pools necessary depending on the implementation. An
 * AlfrescoRepository would contain a session to use.
 * 
 * 
 * @author eric
 * 
 */
public class MongoRetriever extends AbstractArchiveService {
	
	protected Mongo m = null;
	protected DB messageDb;
	protected DBCollection messageColl;
	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.retriever");

	public MongoRetriever(Mongo m, String dbName, String collName) {
		this.m = m;
		try {
			messageDb = m.getDB(dbName);
			messageColl = messageDb.getCollection(collName);
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public Message retrieve(String messageUUID) throws CannotRetrieveException {
		try {
			DBObject messageObject = messageColl.findOne(messageUUID);
			Message message = new MessageImpl();
			setMessageProperties(message, messageObject);
			message.setContentHeader(makeContentHeader(messageObject));

			logger
					.info("Message " + message.getCleanMessageId()
							+ " retrieved");

			return message;
		} catch (IOException e) {
			throw new CannotRetrieveException(e);
		} catch (BadMessageException e) {
			throw new CannotRetrieveException(e);
		}
	}

	@Override
	public MessageSummary retrieveSummary(String messageUUID){

		DBObject messageObject = messageColl.findOne(messageUUID);
		final MessageSummary result = new MessageSummaryImpl();
		result.setId(messageUUID);

		result.setMessageId((String) messageObject.get(MESSAGE_ID));

		final Calendar date = (Calendar) messageObject.get(DATE);
		result.setDate(date.getTime());

		result.setSubject((String) messageObject.get(SUBJECT));

		result.setMailFrom((String) messageObject.get(MAIL_FROM));

		result.setRcptTo((List<String>) messageObject.get(RCPT_TO));

		final String from = (String) messageObject.get(FROM);
		final String fromNoBrackets = Utility
				.stripBracketsFromFromAddress(from);
		result.setFrom(fromNoBrackets);

		result.setTo((List<String>) messageObject.get(TO));

		result.setCc((List<String>) messageObject.get(CC));

		return result;
	}

	private ContentHeader makeContentHeader(DBObject messageObject)
			throws CannotRetrieveException, IOException {
		ContentHeader result = null;
		DBObject contentHeaderObject = (DBObject) messageObject.get("ContentHeader");
		result = makeContentHeaderHelper(contentHeaderObject);
		return result;
	}

	private ContentHeader makeContentHeaderHelper(DBObject contentHeaderObject)
			throws CannotRetrieveException, IOException {
		ContentHeader result = null;

		String nodeTypeName = (String) contentHeaderObject.get("type");
		if (nodeTypeName.equals("SinglePartHeader")) {
			try {
				result = buildContentHeader(contentHeaderObject);
			} catch (DocStoreException e) {
				throw new CannotRetrieveException(
						"Could not retrieve message attachment");
			}
		} else if (nodeTypeName.equals("MultiPartHeader")) {
			result = buildMultiPartHeader(contentHeaderObject);
		} else {
			throw new CannotRetrieveException(
					"expecting a content_header or multipart_header, but got "
							+ nodeTypeName + " instead");
		}

		return result;
	}

	private SinglePartHeader buildContentHeader(DBObject contentHeaderObject)
			throws CannotRetrieveException, IOException, DocStoreException {
		SinglePartHeader header = new SinglePartHeaderImpl();
		setSinglePartHeaderProperties(header, contentHeaderObject);
		
		EncodedContentData encodedContentData = null;
		encodedContentData = buildEncodedContentData((String) contentHeaderObject.get("attachment_id"));
		header.setEncodedContentData(encodedContentData);

		return header;
	}

	private MultiPartHeader buildMultiPartHeader(DBObject contentHeaderObject)
			throws CannotRetrieveException, IOException {
		MultiPartHeader multiPartHeader = new MultiPartHeaderImpl();
		setMultiPartHeaderProperties(multiPartHeader, contentHeaderObject);

		ArrayList<BasicDBObject> partHeadersList = (ArrayList<BasicDBObject>) contentHeaderObject.get("partHeaders");
		for(BasicDBObject partHeaderObject : partHeadersList) {
			ContentHeader partHeader = makeContentHeaderHelper(partHeaderObject);
			multiPartHeader.addPartHeader(partHeader);
		}

		return multiPartHeader;
	}

	private EncodedContentData buildEncodedContentData(String attachmentUUID)
			throws CannotRetrieveException, DocStoreException {
		EncodedContentData encodedContentData = new EncodedContentDataImpl();
		try {
			Document document = docStoreService.retrieve(attachmentUUID);
			encodedContentData.setBinaryContent(document.getContentStream());
		} catch (CannotTransferMessageContentException e) {
			throw new CannotRetrieveException(
					"could not extract data from datastore", e);
		}
		return encodedContentData;
	}

	private void setMessageProperties(Message message, DBObject messageObject)
			throws IOException, BadMessageException {
		BasicDBObject obj = new BasicDBObject();
		message.setOriginalHeaders((String) messageObject.get(ORIGINAL_HEADERS));
	}

	private void setSinglePartHeaderProperties(SinglePartHeader header,
			DBObject headerObject) throws IOException {
		header.setOriginalHeaders((String) headerObject.get(ORIGINAL_HEADERS));

		header.setContentDisposition((String) headerObject.get(CONTENT_DISPOSITION));
		header.setContentTransferEncoding((String) headerObject.get(CONTENT_TRANSFER_ENCODING));
		header.setContentType((String) headerObject.get(CONTENT_TYPE));
		header.setContentID((String) headerObject.get(CONTENT_ID));
	}

	private void setMultiPartHeaderProperties(MultiPartHeader header,
			DBObject headerObject) throws IOException {
		header.setOriginalHeaders((String) headerObject.get(ORIGINAL_HEADERS));

		header.setBoundary((String) headerObject.get(BOUNDARY_ID));
		header.setPreamble((String) headerObject.get(PREAMBLE));
		header.setEpilogue((String) headerObject.get(EPILOGUE));
	}
}