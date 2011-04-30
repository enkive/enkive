package com.linuxbox.enkive.docstore.mongogrid;

import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_EXTENSION_KEY;

import java.io.IOException;
import java.io.InputStream;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentTooLargeException;
import com.mongodb.gridfs.GridFSDBFile;

public class MongoGridDocument implements Document {
	private GridFSDBFile gridFile;
	private byte[] data;

	public MongoGridDocument(GridFSDBFile gridFile) {
		this.gridFile = gridFile;
	}

	@Override
	public byte[] getContentBytes() throws DocStoreException {
		final long lengthL = gridFile.getLength();
		final int length = lengthL <= Integer.MAX_VALUE ? (int) lengthL : -1;
		if (length < 0) {
			throw new DocumentTooLargeException(lengthL, Integer.MAX_VALUE);
		}

		try {
			data = new byte[length];
			gridFile.getInputStream().read(data);
			return data;
		} catch (IOException e) {
			throw new DocStoreException(e);
		}
	}

	@Override
	public InputStream getContentStream() {
		return gridFile.getInputStream();
	}

	@Override
	public String getIdentifier() {
		return gridFile.getFilename();
	}

	@Override
	public String getMimeType() {
		return gridFile.getContentType();
	}

	@Override
	public String getExtension() {
		try {
			return (String) gridFile.getMetaData().get(FILE_EXTENSION_KEY);
		} catch (ClassCastException e) {
			return null;
		}
	}

	@Override
	public long getSize() {
		return gridFile.getLength();
	}
}
