package com.linuxbox.enkive.docstore;

import java.io.InputStream;

import com.linuxbox.enkive.message.AbstractBaseContentData;

/**
 * This class acts as a bridge between the ContentData data type used by the
 * archiver and the more generic Document type used by out back-end storage
 * facility.
 * 
 * @author ivancich
 * 
 */
public class ContentDataDocument implements Document {
	private AbstractBaseContentData contentData;
	private String mimeType;
	private String fileSuffix;

	public ContentDataDocument(AbstractBaseContentData contentData,
			String mimeType, String fileSuffix) {
		this.contentData = contentData;
		this.mimeType = mimeType;
		this.fileSuffix = fileSuffix;
	}

	public ContentDataDocument(AbstractBaseContentData contentData,
			String mimeType) {
		this(contentData, mimeType, null);
	}

	@Override
	public byte[] getContentBytes() {
		return contentData.getByteContent();
	}

	@Override
	public InputStream getContentStream() {
		return contentData.getBinaryContent();
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public String getSuffix() {
		return fileSuffix;
	}
}
