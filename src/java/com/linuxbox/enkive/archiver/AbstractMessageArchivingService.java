package com.linuxbox.enkive.archiver;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.message.Message;

public abstract class AbstractMessageArchivingService implements MessageArchivingService {
	
	protected DocStoreService docStoreService;


	public String storeOrFindMessage(Message message) throws CannotArchiveException{
		String uuid = null;
		uuid = findMessage(message);
		if(uuid == null){
			uuid = storeMessage(message);
		}
		return uuid;
	}
	
	public DocStoreService getDocStoreService() {
		return docStoreService;
	}

	public void setDocStoreService(DocStoreService docStoreService) {
		this.docStoreService = docStoreService;
	}
	
	public static String calculateMessageId(Message message) throws CannotArchiveException {
		String messageUUID = null;
		try {
			MessageDigest sha1calc = MessageDigest.getInstance("SHA-1");
			sha1calc.reset();
			messageUUID = new String(sha1calc.digest(message
					.getReconstitutedEmail().getBytes()));
		} catch (NoSuchAlgorithmException e) {
			throw new CannotArchiveException("Could not calculate UUID for message", e);
		} catch (IOException e) {
			throw new CannotArchiveException("Could not calculate UUID for message", e);
		}
		return messageUUID;
	}
}
