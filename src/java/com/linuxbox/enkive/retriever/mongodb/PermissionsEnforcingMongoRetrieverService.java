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
 ******************************************************************************/
package com.linuxbox.enkive.retriever.mongodb;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CC;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MAIL_FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.RCPT_TO;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.TO;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.ATTACHMENT_ID_LIST;

import java.util.Collection;

import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.AuditServiceException;
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
			if (permService.isAdmin()
					|| permService.canReadMessage(
							permService.getCurrentUsername(), message)) {
				auditService.addEvent(AuditService.MESSAGE_RETRIEVED,
						permService.getCurrentUsername(),
						"Message Retrieved - " + message.getId());
				return message;
			} else
				throw new CannotRetrieveException(
						permService.getCurrentUsername()
								+ " does not have permission to retrieve message: "
								+ messageUUID);
		} catch (CannotGetPermissionsException e) {
			throw new CannotRetrieveException(
					"Could not get permissions for user to retrieve message: "
							+ messageUUID, e);
		} catch (AuditServiceException e) {
			throw new CannotRetrieveException("Could not write to audit log ",
					e);
		}

	}

	@Override
	public EncodedContentData retrieveAttachment(String attachmentUUID)
			throws CannotRetrieveException {
		if (canReadAttachment(permService.getCurrentUsername(), attachmentUUID)) {
			try {
				auditService.addEvent(AuditService.ATTACHMENT_RETRIEVED,
						permService.getCurrentUsername(),
						"Attachment Retrieved - " + attachmentUUID);
			} catch (AuditServiceException e) {
				throw new CannotRetrieveException(
						"Could not write to audit log ", e);
			}
			return super.retrieveAttachment(attachmentUUID);
		} else
			throw new CannotRetrieveException();
	}

	protected boolean canReadAttachment(String userId, String attachmentUUID)
			throws CannotRetrieveException {
		System.out.println("attachment perm 1");
		try {
			if (permService.isAdmin())
				return true;
		} catch (CannotGetPermissionsException e) {
			throw new CannotRetrieveException(
					"Could not get permissions for user " + userId, e);
		}
		BasicDBObject query = new BasicDBObject();

		// Needs to match MAIL_FROM OR FROM
		BasicDBList addressQuery = new BasicDBList();
		Collection<String> addresses = permService.canReadAddresses(userId);
		for (String address : addresses) {
			addressQuery.add(new BasicDBObject(MAIL_FROM, address));
			addressQuery.add(new BasicDBObject(FROM, address));
			addressQuery.add(new BasicDBObject(RCPT_TO, address));
			addressQuery.add(new BasicDBObject(TO, address));
			addressQuery.add(new BasicDBObject(CC, address));
		}
		query.put("$or", addressQuery);
		query.put(ATTACHMENT_ID_LIST, attachmentUUID);
		DBCursor results = messageColl.find(query);
		if (results.size() > 0)
			return true;
		else
			return false;
	}
}
