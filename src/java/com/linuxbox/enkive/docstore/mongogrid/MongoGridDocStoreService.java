package com.linuxbox.enkive.docstore.mongogrid;

import java.net.UnknownHostException;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.EncodedDocument;
import com.linuxbox.enkive.docstore.exceptions.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.exceptions.NoEncodingAvailableException;
import com.linuxbox.enkive.docstore.exceptions.StorageException;
import com.linuxbox.enkive.docstore.exceptions.UnimplementedMethodException;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

public class MongoGridDocStoreService implements DocStoreService {
	private static String BINARY_ENCODING_KEY = "binaryEncoding";
	private static String INDEX_STATUS_KEY = "indexStatus";

	private Mongo mongo;
	private DB db;
	private GridFS gridFS;

	public MongoGridDocStoreService(String host, int port, String dbName,
			String bucketName) throws UnknownHostException, MongoException {
		mongo = new Mongo(host, port);
		db = mongo.getDB(dbName);
		gridFS = new GridFS(db, bucketName);
	}

	@Override
	public Document retrieve(String identifier)
			throws DocumentNotFoundException {
		// TODO Auto-generated method stub
		throw new UnimplementedMethodException();
	}

	@Override
	public EncodedDocument retrieveEncoded(String identifier)
			throws NoEncodingAvailableException {
		// TODO Auto-generated method stub
		throw new UnimplementedMethodException();
	}

	/**
	 * Test whether document is already stored. 
	 */
	@Override
	public String store(Document document) throws StorageException {
		final String identifier = document.getIdentifier();

		GridFSDBFile oldFile = gridFS.findOne(identifier);
		
		if (oldFile == null) {
			doStore(identifier, document);
		}
		
		return identifier;
	}
	
	/**
	 * Actually store the document using the given identifier in the grid FS.
	 * @param identifier
	 * @param document
	 * @throws StorageException
	 */
	private void doStore(String identifier, Document document) throws StorageException {
		GridFSInputFile newFile = gridFS.createFile(document
				.getContentBytes());
		
		newFile.setFilename(identifier);
		newFile.setContentType(document.getMimeType());

		DBObject metaData = newFile.getMetaData();
		metaData.put(INDEX_STATUS_KEY, Boolean.FALSE);

		if (document instanceof EncodedDocument) {
			// store the encoding as meta-data for EncodedDocuments
			EncodedDocument eDoc = (EncodedDocument) document;
			metaData.put(BINARY_ENCODING_KEY, eDoc.getBinaryEncoding());
		}
		
		newFile.setMetaData(metaData);
		newFile.save();	
	}
}
