package com.linuxbox.enkive.archiver;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.archiver.exceptions.MessageArchivingServiceException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.message.Message;

public abstract class AbstractMessageArchivingService implements MessageArchivingService {
	
	protected DocStoreService docStoreService;

	protected abstract void subStartup() throws MessageArchivingServiceException;

	protected abstract void subShutdown() throws MessageArchivingServiceException;

	public void startup() throws MessageArchivingServiceException {
		if (docStoreService == null) {
			throw new MessageArchivingServiceException("indexer queue service not set");
		}

		subStartup();
	}

	public void shutdown() throws MessageArchivingServiceException {
		subShutdown();
	}

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
			messageUUID = new String((new Hex()).encode(sha1calc.digest(message
					.getReconstitutedEmail().getBytes())));
		} catch (NoSuchAlgorithmException e) {
			throw new CannotArchiveException("Could not calculate UUID for message", e);
		} catch (IOException e) {
			throw new CannotArchiveException("Could not calculate UUID for message", e);
		}
		return messageUUID;
	}
}
