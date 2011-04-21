package com.linuxbox.enkive.docstore;

import java.io.IOException;

import com.linuxbox.enkive.docstore.exceptions.DocStoreException;
import com.linuxbox.enkive.message.AbstractBaseContentData;

public class ContentDataEncodedDocument extends EncodedChainedDocument {
	public ContentDataEncodedDocument(AbstractBaseContentData contentData,
			String mimeType, String suffix, String binaryEncoding) throws IOException, DocStoreException {
		super(binaryEncoding, new ContentDataDocument(contentData, mimeType, suffix));
	}
}
