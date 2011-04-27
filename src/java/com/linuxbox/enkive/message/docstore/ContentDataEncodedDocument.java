package com.linuxbox.enkive.message.docstore;

import java.io.IOException;

import com.linuxbox.enkive.docstore.EncodedChainedDocument;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.message.EncodedContentData;

/**
 * This class acts as a bridge between the ContentData data type used by the
 * archiver and the more generic Encoded Document type used by out back-end,
 * document storage service.
 * 
 * @author ivancich
 * 
 */
public class ContentDataEncodedDocument extends EncodedChainedDocument {
	public ContentDataEncodedDocument(EncodedContentData contentData,
			String mimeType, String suffix, String binaryEncoding)
			throws IOException, DocStoreException {
		super(binaryEncoding, new ContentDataDocument(contentData, mimeType,
				suffix));
	}
}
