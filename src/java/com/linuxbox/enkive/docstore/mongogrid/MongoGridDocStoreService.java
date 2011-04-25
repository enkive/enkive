package com.linuxbox.enkive.docstore.mongogrid;

import static com.linuxbox.enkive.docstore.mongogrid.Constants.BINARY_ENCODING_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_SUFFIX_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_STATUS_KEY;

import java.io.IOException;
import java.net.UnknownHostException;

import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.EncodedChainedDocument;
import com.linuxbox.enkive.docstore.EncodedDocument;
import com.linuxbox.enkive.docstore.StoreRequestResult;
import com.linuxbox.enkive.docstore.StoreRequestResultImpl;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.exception.StorageException;
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
		DB db = mongo.getDB(dbName);
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
	public Document retrieve(String identifier) throws DocStoreException {
		GridFSDBFile file = gridFS.findOne(identifier);
		if (file == null) {
			throw new DocumentNotFoundException(identifier);
		}

		Document regularDocument = new MongoGridDocument(file);

		final DBObject metaData = file.getMetaData();

		// if the document is encoded, return an encoded document
		if (metaData.containsField(BINARY_ENCODING_KEY)) {
			try {
				final String binaryEncoding = (String) metaData
						.get(BINARY_ENCODING_KEY);
				return new EncodedChainedDocument(binaryEncoding,
						regularDocument);
			} catch (IOException e) {
				throw new DocStoreException(
						"could not access encoded document", e);
			}
		} else {
			return regularDocument;
		}
	}

	/**
	 * Test whether document is already stored.
	 */
	@Override
	public StoreRequestResult store(Document document) throws DocStoreException {
		final String identifier = document.getIdentifier();

		GridFSDBFile oldFile = gridFS.findOne(identifier);

		if (oldFile == null) {
			doStore(identifier, document);
			return new StoreRequestResultImpl(identifier, false);
		} else {
			return new StoreRequestResultImpl(identifier, true);
		}
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
		GridFSInputFile newFile;
		String binaryEncoding = null;

		if (document instanceof EncodedDocument) {
			System.out.println("storing encoded document");
			EncodedDocument eDoc = (EncodedDocument) document;
			newFile = gridFS.createFile(eDoc.getEncodedContentStream());
			binaryEncoding = eDoc.getBinaryEncoding();
		} else {
			System.out.println("storing decoded document");
			newFile = gridFS.createFile(document.getContentStream());
		}

		newFile.setFilename(identifier);
		newFile.setContentType(document.getMimeType());

		// store the encoding as meta-data for EncodedDocuments
		DBObject metaData = newFile.getMetaData();
		if (metaData == null) {
			metaData = new BasicDBObject();
		}

		metaData.put(INDEX_STATUS_KEY, Boolean.FALSE);
		metaData.put(FILE_SUFFIX_KEY, document.getSuffix());

		if (document instanceof EncodedDocument) {
			metaData.put(BINARY_ENCODING_KEY, binaryEncoding);
		}

		newFile.setMetaData(metaData);
		newFile.save();
	}
	
	boolean markAsIndexedWarningGiven = false;

	@Override
	public void markAsIndexed(String identifier) {
		if (markAsIndexedWarningGiven) return;
		// TODO Auto-generated method stub
		// throw new UnimplementedMethodException();
		System.err.println("MongoGridDocStoreService::markAsIndexed not implemented");
		markAsIndexedWarningGiven = true;
	}

	@Override
	public Document retrieveUnindexed() {
		// TODO Auto-generated method stub
		throw new UnimplementedMethodException();
	}

	@Override
	public void shutdown() {
		getMongo().close();
	}
	
	private DB getDb() {
		return gridFS.getDB();
	}
	
	private Mongo getMongo() {
		return getDb().getMongo();
	}
}
