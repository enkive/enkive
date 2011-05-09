package com.linuxbox.enkive.message.docstore;

import java.io.InputStream;

import com.linuxbox.enkive.docstore.AbstractDocument;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.message.EncodedContentData;

/**
 * This class acts as a bridge between the ContentData data type used by the
 * archiver and the more generic Document type used by out back-end,
 * document storage service.
 * 
 * @author ivancich
 * 
 */
public class ContentDataDocument extends AbstractDocument {
	private EncodedContentData contentData;

	public ContentDataDocument(EncodedContentData contentData,
			String mimeType, String fileSuffix, String binaryEncoding) {
		super(mimeType, fileSuffix, binaryEncoding);
		this.contentData = contentData;
	}
	
	/**
	 * Do not know the size, so return negative value.
	 */
	@Override
	public long getEncodedSize() {
		return -1;
	}

	@Override
	public InputStream getEncodedContentStream() {
		return contentData.getBinaryContent();
	}
	
	@Override
	public InputStream getDecodedContentStream() throws DocStoreException {
		throw new DocStoreException("decoded version of ContentData not available");
	}
}
