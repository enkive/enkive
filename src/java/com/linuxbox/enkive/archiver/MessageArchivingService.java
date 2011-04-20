package com.linuxbox.enkive.archiver;

import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.message.Message;

public interface MessageArchivingService {
	
	public String storeOrFindMessage(Message message) throws CannotArchiveException;
	
	public String storeMessage(Message message) throws CannotArchiveException;
	
	public String findMessage(Message message) throws CannotArchiveException;
	
}
