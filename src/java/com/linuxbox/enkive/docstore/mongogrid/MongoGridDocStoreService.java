package com.linuxbox.enkive.docstore.mongogrid;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.EncodedDocument;
import com.linuxbox.enkive.docstore.exceptions.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.exceptions.NoEncodingAvailableException;
import com.linuxbox.enkive.docstore.exceptions.StorageException;

public class MongoGridDocStoreService implements DocStoreService {

	@Override
	public Document retrieve(String identifier)
			throws DocumentNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EncodedDocument retrieveEncoded(String identifier)
			throws NoEncodingAvailableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String store(Document document) throws StorageException {
		// TODO Auto-generated method stub
		return null;
	}
}
