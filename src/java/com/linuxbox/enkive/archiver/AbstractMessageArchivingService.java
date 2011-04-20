package com.linuxbox.enkive.archiver;

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
	
}
