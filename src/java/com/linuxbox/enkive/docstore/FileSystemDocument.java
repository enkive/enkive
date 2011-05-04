package com.linuxbox.enkive.docstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.linuxbox.enkive.docstore.exception.DocStoreException;

public class FileSystemDocument extends AbstractDocument {
	private File theFile;

	public FileSystemDocument(String path, String mimeType,
			String fileExtension, String binaryEncoding) {
		super(mimeType, fileExtension, binaryEncoding);
		theFile = new File(path);
	}

	@Override
	public long getEncodedSize() {
		return theFile.length();
	}

	@Override
	public InputStream getEncodedContentStream() throws DocStoreException {
		try {
			FileInputStream fileStream = new FileInputStream(theFile);
			return fileStream;
		} catch (IOException e) {
			throw new DocStoreException(e);
		}
	}
}
