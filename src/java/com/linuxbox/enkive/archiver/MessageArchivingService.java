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
package com.linuxbox.enkive.archiver;

import java.io.IOException;

import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.archiver.exceptions.FailedToEmergencySaveException;
import com.linuxbox.enkive.archiver.exceptions.MessageArchivingServiceException;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.message.Message;
import com.mongodb.MongoException;

public interface MessageArchivingService {
	/**
	 * Stores the given message and generates a unique identifier for the
	 * message, which is returned. If the message is already stored, it is not
	 * stored a second time, but instead the existing identifier is returned.
	 * This method does de-duplication.
	 * 
	 * @param message
	 * @return unique identifier for message
	 * @throws CannotArchiveException
	 * @throws IOException
	 * @throws AuditServiceException
	 * @throws FailedToEmergencySaveException
	 */
	public String storeOrFindMessage(Message message)
			throws CannotArchiveException,
			FailedToEmergencySaveException,
			AuditServiceException,
			IOException,
			com.linuxbox.enkive.archiver.exceptions.FailedToEmergencySaveException;

	/**
	 * Stores the given message and generates a unique identifier for the
	 * message, which is returned.
	 * 
	 * @param message
	 * @return unique identifier for message
	 * @throws CannotArchiveException
	 * @throws AuditServiceException
	 * @throws FailedToEmergencySaveException
	 * @throws IOException
	 */
	public String storeMessage(Message message) throws CannotArchiveException,
			FailedToEmergencySaveException, AuditServiceException, IOException;

	/**
	 * Searches the message store for a duplicate message. If a duplicate is
	 * found the unique id for that message is returned. If no duplicates are
	 * found, null is returned.
	 * 
	 * @param message
	 * @return unique identifier for message
	 * @throws CannotArchiveException
	 * @throws MongoException
	 */
	public String findMessage(Message message) throws MongoException,
			CannotArchiveException;

	public boolean emergencySave(final String data, boolean messageIsIncomplete)
			throws FailedToEmergencySaveException, AuditServiceException;

	public boolean emergencySave(final String data)
			throws FailedToEmergencySaveException, AuditServiceException;

	/**
	 * Removes a message from the store. This method will remove attachments
	 * utilizing the underlying Document Storage Service's removal method.
	 * 
	 * @param messageUUID
	 * @return boolean reflecting success or failure of deletion
	 */
	public boolean removeMessage(String messageUUID);

	/**
	 * Initialize any resources needed for message storage
	 * 
	 * @throws MessageArchivingServiceException
	 */
	public void startup() throws MessageArchivingServiceException;

	/**
	 * Clean up any resources needed for message storage
	 * 
	 * @throws MessageArchivingServiceException
	 */
	public void shutdown() throws MessageArchivingServiceException;

}
