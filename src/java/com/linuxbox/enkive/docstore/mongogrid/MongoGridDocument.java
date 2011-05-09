package com.linuxbox.enkive.docstore.mongogrid;

import java.io.InputStream;

import com.linuxbox.enkive.docstore.AbstractDocument;
import com.mongodb.gridfs.GridFSDBFile;

public class MongoGridDocument extends AbstractDocument {
	private GridFSDBFile gridFile;

	public MongoGridDocument(GridFSDBFile gridFile) {
		super(gridFile.getContentType(), (String) gridFile.getMetaData().get(
				Constants.FILE_EXTENSION_KEY), (String) gridFile.getMetaData()
				.get(Constants.BINARY_ENCODING_KEY));
		this.gridFile = gridFile;
	}

	@Override
	public InputStream getEncodedContentStream() {
		return gridFile.getInputStream();
	}

	@Override
	public long getEncodedSize() {
		return gridFile.getLength();
	}
}
