package com.linuxbox.enkive.message.docstore;

import java.io.InputStream;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.message.AbstractEncodedReadData;

public class DocumentEncodedContentData extends AbstractEncodedReadData {
	protected Document document;
	
	public DocumentEncodedContentData(String uuid, Document document) {
		super(uuid, document.getFilename(), document.getMimeType());
		this.document = document;
	}

	@Override
	public InputStream getBinaryContent() throws DocStoreException {
		return document.getDecodedContentStream();
	}
}
