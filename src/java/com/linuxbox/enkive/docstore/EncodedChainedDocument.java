package com.linuxbox.enkive.docstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.james.mime4j.codec.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.apache.james.mime4j.util.MimeUtil;

import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.UnknownEncodingFormatException;
import com.linuxbox.util.StreamConnector;

public class EncodedChainedDocument extends InMemoryDocument implements
		EncodedDocument {
	private String binaryEncoding;
	private byte[] decodedData;

	public EncodedChainedDocument(String binaryEncoding, Document document)
			throws IOException, DocStoreException {
		super(document.getIdentifier(), document.getMimeType(), document
				.getSuffix(), document.getContentStream());
		this.binaryEncoding = binaryEncoding;
	}

	@Override
	public String getEncodedContentString() throws DocStoreException {
		return new String(getContentBytes());
	}

	@Override
	public char[] getEncodedContentChars() throws DocStoreException {
		return getEncodedContentString().toCharArray();
	}

	@Override
	public Reader getEncodedContentReader() throws DocStoreException {
		return new InputStreamReader(getContentStream());
	}

	@Override
	public InputStream getEncodedContentStream() throws DocStoreException {
		return new ByteArrayInputStream(super.getContentBytes());
	}

	@Override
	public byte[] getContentBytes() throws DocStoreException {
		if (decodedData == null) {
			decodedData = decodeContent();
		}
		return decodedData;
	}

	@Override
	public InputStream getContentStream() throws DocStoreException {
		return new ByteArrayInputStream(getContentBytes());
	}

	private byte[] decodeContent() throws DocStoreException {
		try {
			InputStream inputStream;
			if (MimeUtil.isBase64Encoding(binaryEncoding)) {
				inputStream = new Base64InputStream(super.getContentStream());
			} else if (MimeUtil.isQuotedPrintableEncoded(binaryEncoding)) {
				inputStream = new QuotedPrintableInputStream(super
						.getContentStream());
			} else {
				if (binaryEncoding == null || binaryEncoding.isEmpty()) {
					throw new UnknownEncodingFormatException();
				} else {
					throw new UnknownEncodingFormatException(binaryEncoding);
				}
			}

			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			StreamConnector.transferForeground(inputStream, byteStream);

			return byteStream.toByteArray();
		} catch (IOException e) {
			throw new DocStoreException(
					"could not decode encoded binary stream", e);
		}
	}

	@Override
	public String getBinaryEncoding() {
		return binaryEncoding;
	}
}
