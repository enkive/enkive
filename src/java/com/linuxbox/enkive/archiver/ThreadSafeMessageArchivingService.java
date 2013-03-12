/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
/*
 * 
 */
package com.linuxbox.enkive.archiver;

import java.io.IOException;
import java.util.ConcurrentModificationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.archiver.exceptions.FailedToEmergencySaveException;
import com.linuxbox.enkive.archiver.exceptions.MessageArchivingServiceException;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.docstore.DocStoreConstants;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.util.lockservice.LockService;
import com.linuxbox.util.lockservice.LockServiceException;
import com.mongodb.MongoException;

public class ThreadSafeMessageArchivingService implements
		MessageArchivingService {

	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.messageArchvingService");
	protected MessageArchivingService messageArchivingService;
	/*
	 * These are used to retry archiving.
	 */
	protected LockService lockService;
	private final static int RETRIES = 10;
	private final static long RETRY_DELAY_MILLISECONDS = 10000;

	@Override
	public String storeOrFindMessage(Message message)
			throws CannotArchiveException, FailedToEmergencySaveException,
			AuditServiceException, IOException, FailedToEmergencySaveException {
		String uuid = null;
		String calculatedMessageId = AbstractMessageArchivingService
				.calculateMessageId(message);
		try {
			uuid = findMessage(message);
			if (uuid == null) {
				ConcurrentModificationException lastException = null;
				for (int i = 0; i < RETRIES; i++) {
					lastException = null;
					try {
						lockService.lockWithRetries(calculatedMessageId,
								DocStoreConstants.LOCK_TO_STORE, RETRIES,
								RETRY_DELAY_MILLISECONDS);

						uuid = storeMessage(message);
						if (uuid != null)
							return uuid;
					} catch (ConcurrentModificationException e) {
						lastException = e;
					} finally {
						lockService.releaseLock(calculatedMessageId);
					}
					try {
						Thread.sleep(RETRY_DELAY_MILLISECONDS);
					} catch (InterruptedException e) {
						throw new CannotArchiveException(
								"thread interrupted while trying to archive message",
								e);
					}
				}
				if (lastException != null) {
					throw lastException;
				}
			}
		} catch (Exception e) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error(
						"Could not archive Message "
								+ message.getCleanMessageId(), e);
			emergencySave(message.getReconstitutedEmail());
		}
		return uuid;
	}

	@Override
	public String storeMessage(Message message) throws CannotArchiveException,
			FailedToEmergencySaveException, AuditServiceException, IOException {
		return messageArchivingService.storeMessage(message);
	}

	@Override
	public String findMessage(Message message) throws MongoException,
			CannotArchiveException {
		return messageArchivingService.findMessage(message);
	}

	@Override
	public boolean removeMessage(String messageUUID) {
		boolean messageRemoved = false;
		try {
			lockService.lock(messageUUID, DocStoreConstants.LOCK_TO_REMOVE);
			messageRemoved = messageArchivingService.removeMessage(messageUUID);
			lockService.releaseLock(messageUUID);
		} catch (LockServiceException e) {
			LOGGER.warn(
					"A lockservice error occurred while removing a message", e);
		}
		return messageRemoved;
	}

	public MessageArchivingService getMessageArchivingService() {
		return messageArchivingService;
	}

	public void setMessageArchivingService(
			MessageArchivingService messageArchivingService) {
		this.messageArchivingService = messageArchivingService;
	}

	public LockService getLockService() {
		return lockService;
	}

	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	@Override
	public boolean emergencySave(String data, boolean messageIsIncomplete)
			throws FailedToEmergencySaveException, AuditServiceException {
		return messageArchivingService.emergencySave(data, messageIsIncomplete);
	}

	@Override
	public boolean emergencySave(String data)
			throws FailedToEmergencySaveException, AuditServiceException {
		return messageArchivingService.emergencySave(data);
	}

	@Override
	public void startup() throws MessageArchivingServiceException {

	}

	@Override
	public void shutdown() throws MessageArchivingServiceException {

	}

}
