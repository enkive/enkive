package com.linuxbox.enkive.docstore.mongogrid;

import static com.linuxbox.enkive.docstore.mongogrid.Constants.BINARY_ENCODING_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_SUFFIX_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_STATUS_KEY;

import java.net.UnknownHostException;

import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.EncodedDocument;
import com.linuxbox.enkive.docstore.exceptions.DocStoreException;
import com.linuxbox.enkive.docstore.exceptions.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.exceptions.NoEncodingAvailableException;
import com.linuxbox.enkive.docstore.exceptions.StorageException;
import com.linuxbox.enkive.exception.UnimplementedMethodException;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

public class MongoGridDocStoreService implements DocStoreService {
	private Mongo mongo;
	private DB db;
	private GridFS gridFS;

	public MongoGridDocStoreService(String host, int port, String dbName,
			String bucketName) throws UnknownHostException {
		this(new Mongo(host, port), dbName, bucketName);
	}

	public MongoGridDocStoreService(String host, String dbName,
			String bucketName) throws UnknownHostException {
		this(new Mongo(host), dbName, bucketName);
	}

	public MongoGridDocStoreService(String dbName, String bucketName)
			throws UnknownHostException {
		this(new Mongo(), dbName, bucketName);
	}

	public MongoGridDocStoreService(Mongo mongo, String dbName,
			String bucketName) {
		this.mongo = mongo;
		db = this.mongo.getDB(dbName);
		gridFS = new GridFS(db, bucketName);

		DBCollection fileCollection = gridFS.getDB().getCollection(
				bucketName + ".files");

		DBObject filenameIndex = BasicDBObjectBuilder.start()
				.add("filename", 1).get();
		fileCollection.createIndex(filenameIndex);

		DBObject searchIndexingIndex = BasicDBObjectBuilder.start().add(
				"uploadDate", 1).add("metadata." + INDEX_STATUS_KEY, 1).get();
		fileCollection.createIndex(searchIndexingIndex);
	}

	@Override
	public Document retrieve(String identifier)
			throws DocumentNotFoundException {
		GridFSDBFile file = gridFS.findOne(identifier);
		if (file == null) {
			throw new DocumentNotFoundException(identifier);
		}
		
		return new MongoGridDocument(file);
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
	public String store(Document document) throws DocStoreException {
		final String identifier = document.getIdentifier();

		GridFSDBFile oldFile = gridFS.findOne(identifier);

		if (oldFile == null) {
			doStore(identifier, document);
		}

		return identifier;
	}

	/**
	 * Actually store the document using the given identifier in the grid FS.
	 * 
	 * @param identifier
	 * @param document
	 * @throws StorageException
	 */
	private void doStore(String identifier, Document document)
			throws DocStoreException {
		GridFSInputFile newFile = gridFS.createFile(document.getContentBytes());

		newFile.setFilename(identifier);
		newFile.setContentType(document.getMimeType());

		DBObject metaData = newFile.getMetaData();
		if (metaData == null) {
			metaData = new BasicDBObject();
		}

		metaData.put(INDEX_STATUS_KEY, Boolean.FALSE);
		metaData.put(FILE_SUFFIX_KEY, document.getSuffix());

		if (document instanceof EncodedDocument) {
			// store the encoding as meta-data for EncodedDocuments
			EncodedDocument eDoc = (EncodedDocument) document;
			metaData.put(BINARY_ENCODING_KEY, eDoc.getEncodedContentString());
		}

		newFile.setMetaData(metaData);
		newFile.save();
	}
}
