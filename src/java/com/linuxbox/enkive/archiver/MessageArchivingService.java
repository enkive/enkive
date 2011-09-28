package com.linuxbox.enkive.archiver;

import java.io.IOException;

import com.linuxbox.enkive.archiver.exceptions.FailedToEmergencySaveException;
import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
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
	 */
	public String storeMessage(Message message) throws CannotArchiveException, FailedToEmergencySaveException, AuditServiceException;

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
	 * Removes a message from the store. Note that this method does not remove
	 * attachments.
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
