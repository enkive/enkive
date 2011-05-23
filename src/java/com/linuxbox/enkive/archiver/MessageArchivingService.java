package com.linuxbox.enkive.archiver;

import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
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
	 */
	public String storeOrFindMessage(Message message) throws CannotArchiveException;
	
	/**
	 * Stores the given message and generates a unique identifier for the
	 * message, which is returned.
	 * 
	 * @param message
	 * @return unique identifier for message
	 * @throws CannotArchiveException
	 */
	public String storeMessage(Message message) throws CannotArchiveException;
	
	/**
	 * Searches the message store for a duplicate message.
	 * If a duplicate is found the unique id for that message is returned.
	 * If no duplicates are found, null is returned.
	 * 
	 * @param message
	 * @return unique identifier for message
	 * @throws CannotArchiveException 
	 * @throws MongoException 
	 */
	public String findMessage(Message message) throws MongoException, CannotArchiveException;
	
	/**
	 * Removes a message from the store. Note that this method does not remove
	 * attachments.
	 * 
	 * @param messageUUID
	 * @return boolean reflecting success or failure of deletion
	 */
	public boolean removeMessage(String messageUUID);
	
}
