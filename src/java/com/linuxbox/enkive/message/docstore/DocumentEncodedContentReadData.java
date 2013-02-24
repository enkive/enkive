package com.linuxbox.enkive.message.docstore;

import java.io.InputStream;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.message.AbstractEncodedReadData;
import com.linuxbox.enkive.message.ContentException;

public class DocumentEncodedContentReadData extends AbstractEncodedReadData {
	protected Document document;

	public DocumentEncodedContentReadData(String uuid, Document document) {
		super(uuid, document.getFilename(), document.getMimeType());
		this.document = document;
	}

	@Override
	public InputStream getEncodedContent() throws ContentException {
		try {
			return document.getEncodedContentStream();
		} catch (DocStoreException e) {
			throw new ContentException(e);
		}
	}

	@Override
	public InputStream getBinaryContent() throws ContentException {
		try {
			return document.getDecodedContentStream();
		} catch (DocStoreException e) {
			throw new ContentException(e);
		}
	}
}
