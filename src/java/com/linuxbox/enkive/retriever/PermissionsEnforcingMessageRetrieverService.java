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
/*
 * 
 */
package com.linuxbox.enkive.retriever;

import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.EncodedContentData;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageSummary;
import com.linuxbox.enkive.permissions.PermissionService;

public class PermissionsEnforcingMessageRetrieverService extends
		AbstractRetrieverService implements MessageRetrieverService {

	PermissionService permService;
	MessageRetrieverService retrieverService;
	AuditService auditService;

	@Override
	public MessageSummary retrieveSummary(String messageUUID)
			throws CannotRetrieveException {
		MessageSummary message = retrieverService.retrieveSummary(messageUUID);
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
	public Message retrieve(String messageUUID) throws CannotRetrieveException {
		Message message = retrieverService.retrieve(messageUUID);
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
		try {
			if (permService.canReadAttachment(permService.getCurrentUsername(),
					attachmentUUID)) {
				try {
					auditService.addEvent(AuditService.ATTACHMENT_RETRIEVED,
							permService.getCurrentUsername(),
							"Attachment Retrieved - " + attachmentUUID);
				} catch (AuditServiceException e) {
					throw new CannotRetrieveException(
							"Could not write to audit log ", e);
				}
				return retrieverService.retrieveAttachment(attachmentUUID);
			} else
				throw new CannotRetrieveException(
						"User does not have permissions to read attachment "
								+ attachmentUUID);
		} catch (CannotGetPermissionsException e) {
			throw new CannotRetrieveException(
					"Could not get permissions for user "
							+ permService.getCurrentUsername()
							+ " to read attachment " + attachmentUUID);
		}
	}

	public PermissionService getPermService() {
		return permService;
	}

	public void setPermService(PermissionService permService) {
		this.permService = permService;
	}

	public MessageRetrieverService getRetrieverService() {
		return retrieverService;
	}

	public void setRetrieverService(MessageRetrieverService retrieverService) {
		this.retrieverService = retrieverService;
	}

	public AuditService getAuditService() {
		return auditService;
	}

	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

}
