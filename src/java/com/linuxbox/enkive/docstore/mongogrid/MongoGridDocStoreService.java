package com.linuxbox.enkive.docstore.mongogrid;

import static com.linuxbox.enkive.docstore.mongogrid.Constants.BINARY_ENCODING_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILENAME_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_EXTENSION_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_STATUS_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_STATUS_QUERY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_TIMESTAMP_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_TIMESTAMP_QUERY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.OBJECT_ID_KEY;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.EncodedChainedDocument;
import com.linuxbox.enkive.docstore.EncodedDocument;
import com.linuxbox.enkive.docstore.StoreRequestResult;
import com.linuxbox.enkive.docstore.StoreRequestResultImpl;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.exception.StorageException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.QueryBuilder;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

public class MongoGridDocStoreService implements DocStoreService {
	/*
	 * The various status value a document can have to indicate its state of
	 * indexing.
	 */
	private static final int STATUS_UNINDEXED = 0;
	private static final int STATUS_INDEXING = 1;
	private static final int STATUS_INDEXED = 2;
	private static final int STATUS_STALE = 3;

	private GridFS gridFS;
	private DBCollection filesCollection;

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

		filesCollection = gridFS.getDB().getCollection(bucketName + ".files");

		DBObject filenameIndex = BasicDBObjectBuilder.start()
				.add("filename", 1).get();
		filesCollection.createIndex(filenameIndex);

		DBObject searchIndexingIndex = BasicDBObjectBuilder.start().add(
				INDEX_STATUS_KEY, 1).add(INDEX_TIMESTAMP_KEY, 1).get();
		filesCollection.createIndex(searchIndexingIndex);
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

		metaData.put(INDEX_STATUS_KEY, STATUS_UNINDEXED);
		metaData.put(FILE_EXTENSION_KEY, document.getExtension());

		if (document instanceof EncodedDocument) {
			metaData.put(BINARY_ENCODING_KEY, binaryEncoding);
		}

		newFile.setMetaData(metaData);
		newFile.save();
	}

	@Override
	public void markAsIndexed(String identifier)
			throws DocumentNotFoundException {
		GridFSDBFile file = gridFS.findOne(identifier);
		if (file == null) {
			throw new DocumentNotFoundException(identifier);
		}

		final DBObject metaData = file.getMetaData();
		metaData.put(INDEX_STATUS_KEY, STATUS_INDEXED);
		metaData.put(INDEX_TIMESTAMP_KEY, new Date());
		file.setMetaData(metaData);
		// file.save();
	}

	final static DBObject SORT_BY_INDEX_TIMESTAMP = new BasicDBObject(
			INDEX_TIMESTAMP_QUERY, 1);
	final static DBObject UNINDEXED_QUERY = new QueryBuilder().and(
			INDEX_STATUS_QUERY).is(STATUS_UNINDEXED).get();
	final static DBObject RETRIEVE_OBJECT_ID_AND_FILENAME = BasicDBObjectBuilder
			.start().add(OBJECT_ID_KEY, 1).add(FILENAME_KEY, 1).get();

	@Override
	public String nextUnindexed() {
		final DBObject updateToIndexingOld = BasicDBObjectBuilder.start().add(
				INDEX_STATUS_QUERY, STATUS_INDEXING).add(INDEX_TIMESTAMP_QUERY,
				new Date()).get();

		final BasicDBList updateToIndexing = new BasicDBList();
		updateToIndexing.add(new BasicDBObject("$set", new BasicDBObject(
				INDEX_STATUS_QUERY, STATUS_INDEXING)));
		updateToIndexing.add(new BasicDBObject("$set", new BasicDBObject(
				INDEX_TIMESTAMP_QUERY, new Date())));

		/*
		 * Constants.METADATA_KEY, BasicDBObjectBuilder.start().add(
		 * INDEX_STATUS_KEY, STATUS_INDEXING).add( INDEX_TIMESTAMP_KEY, new
		 * Date()).get());
		 */

		System.out.println(updateToIndexing);

		DBObject result = filesCollection.findAndModify(UNINDEXED_QUERY,
				RETRIEVE_OBJECT_ID_AND_FILENAME, SORT_BY_INDEX_TIMESTAMP,
				false, updateToIndexing, true, false);

		if (result != null) {
			return (String) result.get(FILENAME_KEY);
		} else {
			return null;
		}
	}

	/*
	 * incorrect way of doing it
	 * 
	 * DBCursor cursor = filesCollection.find(UNINDEXED_QUERY, new
	 * BasicDBObject(Constants.FILENAME_KEY, 1)).sort( new
	 * BasicDBObject(Constants.METADATA_KEY, new BasicDBObject(
	 * Constants.INDEX_TIMESTAMP_KEY, 1))).limit(1); if (!cursor.hasNext()) {
	 * return null; }
	 * 
	 * DBObject result = cursor.next(); String identifier = (String)
	 * result.get(Constants.FILENAME_KEY); cursor.close(); if (identifier ==
	 * null) { return null; }
	 * 
	 * GridFSDBFile file = gridFS.findOne(identifier); if (file == null) {
	 * return null; } else { final DBObject metaData = file.getMetaData();
	 * metaData.put(INDEX_STATUS_KEY, STATUS_INDEXING);
	 * metaData.put(INDEX_TIMESTAMP_KEY, new Date());
	 * file.setMetaData(metaData); file.save(); return file.getFilename(); } }
	 */

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
