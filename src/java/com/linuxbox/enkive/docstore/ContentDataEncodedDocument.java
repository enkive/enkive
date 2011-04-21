package com.linuxbox.enkive.docstore;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import com.linuxbox.enkive.exception.UnimplementedMethodException;
import com.linuxbox.enkive.message.AbstractBaseContentData;

public class ContentDataEncodedDocument extends ContentDataDocument implements
		EncodedDocument {
	private String binaryEncoding;
	private String dataString;
	private Charset charSet; 

	public ContentDataEncodedDocument(AbstractBaseContentData contentData,
			String mimeType, String binaryEncoding, Charset charSet) {
		super(contentData, mimeType);
		this.binaryEncoding = binaryEncoding;
		this.charSet = charSet;
	}

	/**
	 * This should likely return the DECODED binary form.
	 */
	@Override
	public byte[] getContentBytes() {
		throw new UnimplementedMethodException();
	}
	
	/**
	 * This should likely return the DECODED binary form.
	 */
	@Override
	public InputStream getContentStream() {
		throw new UnimplementedMethodException();
	}

	@Override
	public char[] getEncodedContentChars() {
		setDataString();
		return dataString.toCharArray();
	}
	
	@Override
	public String getEncodedContentString() {
		setDataString();
		return dataString;
	}

	@Override
	public Reader getEncodedContentReader() {
		return new InputStreamReader(getContentStream());
	}

	@Override
	public String getBinaryEncoding() {
		return binaryEncoding;
	}
	
	private void setDataString() {
		if (dataString == null) {
			dataString = new String(super.getContentBytes(), charSet);
		}
	}

	@Override
	public Charset getCharset() {
		return charSet;
	}
}
