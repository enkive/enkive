package com.linuxbox.enkive.docstore;

import java.io.InputStream;

import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.apache.james.mime4j.util.MimeUtil;

import com.linuxbox.enkive.docstore.exception.DocStoreException;

/**
 * Provides most of the implementation for a Document. All the subclass really
 * needs to do is supply getEncodedContentStream.
 * 
 * @author ivancich
 * 
 */
public abstract class AbstractDocument implements Document {
	protected String mimeType;
	protected String fileExtension;
	protected String binaryEncoding;
	protected String filename;

	public AbstractDocument(String mimeType, String filename, String fileExtension,
			String binaryEncoding) {
		this.mimeType = mimeType;
		this.fileExtension = fileExtension;
		this.binaryEncoding = binaryEncoding;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public String getFileExtension() {
		return fileExtension;
	}

	@Override
	public String getBinaryEncoding() {
		return binaryEncoding;
	}

	@Override
	public InputStream getDecodedContentStream() throws DocStoreException {
		if (MimeUtil.isBase64Encoding(binaryEncoding)) {
			return new Base64InputStream(getEncodedContentStream());
		} else if (MimeUtil.isQuotedPrintableEncoded(binaryEncoding)) {
			return new QuotedPrintableInputStream(getEncodedContentStream());
		} else {
			return getEncodedContentStream();
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}