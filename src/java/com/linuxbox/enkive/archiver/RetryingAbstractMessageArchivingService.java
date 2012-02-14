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
package com.linuxbox.enkive.archiver;

import java.io.IOException;
import java.util.ConcurrentModificationException;

import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.archiver.exceptions.FailedToEmergencySaveException;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.message.Message;

public abstract class RetryingAbstractMessageArchivingService extends
		AbstractMessageArchivingService {

	/*
	 * These are used to retry archiving.
	 */
	private final static int RETRIES = 10;
	private final static long RETRY_DELAY_MILLISECONDS = 10000;

	@Override
	public String storeOrFindMessage(Message message)
			throws CannotArchiveException, FailedToEmergencySaveException,
			AuditServiceException, IOException {
		String uuid = null;
		try {
			uuid = findMessage(message);
			if (uuid == null) {
				ConcurrentModificationException lastException = null;
				for (int i = 0; i < RETRIES; i++) {
					lastException = null;
					try {
						uuid = storeMessage(message);
						if (uuid != null)
							return uuid;
					} catch (ConcurrentModificationException e) {
						lastException = e;
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
			logger.error(
					"Could not archive Message " + message.getCleanMessageId(),
					e);
			emergencySave(message.getReconstitutedEmail());
		}
		return uuid;
	}
}
