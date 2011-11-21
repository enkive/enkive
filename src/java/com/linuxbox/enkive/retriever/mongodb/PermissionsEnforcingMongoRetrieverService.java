package com.linuxbox.enkive.retriever.mongodb;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CC;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MAIL_FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.RCPT_TO;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.TO;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.ATTACHMENT_ID_LIST;

import java.util.Collection;

import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.EncodedContentData;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.permissions.PermissionService;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

public class PermissionsEnforcingMongoRetrieverService extends
		MongoRetrieverService {

	PermissionService permService;

	public PermissionsEnforcingMongoRetrieverService(
			PermissionService permService, Mongo m, String dbName,
			String collName) {
		super(m, dbName, collName);
		this.permService = permService;
	}

	@Override
	public Message retrieve(String messageUUID) throws CannotRetrieveException {
		Message message = super.retrieve(messageUUID);
		try {
			if (permService.isAdmin() || permService.canReadMessage(permService.getCurrentUsername(),
					message))
				return message;
			else
				throw new CannotRetrieveException(
						permService.getCurrentUsername()
								+ " does not have permission to retrieve message: "
								+ messageUUID);
		} catch (CannotGetPermissionsException e) {
			throw new CannotRetrieveException(
					"Could not get permissions for user to retrieve message: "
							+ messageUUID, e);
		}

	}

	@Override
	public EncodedContentData retrieveAttachment(String attachmentUUID)
			throws CannotRetrieveException {
		if (canReadAttachment(permService.getCurrentUsername(), attachmentUUID))
			return super.retrieveAttachment(attachmentUUID);
		else
			throw new CannotRetrieveException();
	}

	protected boolean canReadAttachment(String userId, String attachmentUUID) {

		if(permService.isAdmin())
			return true;
		BasicDBObject query = new BasicDBObject();

		// Needs to match MAIL_FROM OR FROM
		BasicDBList senderQuery = new BasicDBList();

		Collection<String> addresses = permService.canReadAddresses(userId);
		for (String address : addresses) {
			senderQuery.add(new BasicDBObject(MAIL_FROM, address));
			senderQuery.add(new BasicDBObject(FROM, address));
		}
		query.put("$or", senderQuery);

		BasicDBList receiverQuery = new BasicDBList();

		for (String address : addresses) {
			receiverQuery.add(new BasicDBObject(RCPT_TO, address));
			receiverQuery.add(new BasicDBObject(TO, address));
			receiverQuery.add(new BasicDBObject(CC, address));
		}
		query.put("$or", receiverQuery);
		query.put(ATTACHMENT_ID_LIST, attachmentUUID);

		DBCursor results = messageColl.find(query);
		if (results.size() > 0)
			return true;
		else
			return false;
	}
}
