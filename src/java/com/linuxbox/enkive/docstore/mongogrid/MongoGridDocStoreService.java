package com.linuxbox.enkive.docstore.mongogrid;

import static com.linuxbox.enkive.docstore.mongogrid.Constants.BINARY_ENCODING_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.CONT_FILE_COLLECTION;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.CONT_FILE_IDENTIFIER_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILENAME_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_EXTENSION_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_STATUS_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_STATUS_QUERY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_TIMESTAMP_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_TIMESTAMP_QUERY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.OBJECT_ID_KEY;

import java.io.ByteArrayInputStream;
import java.net.UnknownHostException;
import java.util.Date;

import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.AbstractDocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.StoreRequestResult;
import com.linuxbox.enkive.docstore.StoreRequestResultImpl;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentNotFoundException;
import com.linuxbox.util.HashingInputStream;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/*
 * NOTE: we may have some challenges w.r.t. atomicity using MongoDB for a filestore.
 * Please see:
 * http://groups.google.com/group/mongodb-user/browse_thread/thread/4a7419e07c73537/2931a3163836d6ba
 */

public class MongoGridDocStoreService extends AbstractDocStoreService {
	/*
	 * The various status value a document can have to indicate its state of
	 * indexing.
	 */
	private static final int STATUS_UNINDEXED = 0;
	private static final int STATUS_INDEXING = 1;
	private static final int STATUS_INDEXED = 2;
	@SuppressWarnings("unused")
	private static final int STATUS_STALE = 3;

	final static DBObject SORT_BY_INDEX_TIMESTAMP = new BasicDBObject(
			INDEX_TIMESTAMP_QUERY, 1);
	final static DBObject UNINDEXED_QUERY = new QueryBuilder()
			.and(INDEX_STATUS_QUERY).is(STATUS_UNINDEXED).get();
	final static DBObject RETRIEVE_OBJECT_ID = BasicDBObjectBuilder.start()
			.add(OBJECT_ID_KEY, 1).get();
	final static DBObject RETRIEVE_OBJECT_ID_AND_FILENAME = BasicDBObjectBuilder
			.start().add(OBJECT_ID_KEY, 1).add(FILENAME_KEY, 1).get();

	private GridFS gridFS;
	private DBCollection filesCollection;
	private DBCollection fileControlCollection;

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

		// files collection

		filesCollection = gridFS.getDB().getCollection(bucketName + ".files");

		// TODO: does this index already exist? Should it be made unique?
		DBObject filenameIndex = BasicDBObjectBuilder.start()
				.add(FILENAME_KEY, 1).get();
		filesCollection.ensureIndex(filenameIndex);

		DBObject searchIndexingIndex = BasicDBObjectBuilder.start()
				.add(INDEX_STATUS_KEY, 1).add(INDEX_TIMESTAMP_KEY, 1).get();
		filesCollection.ensureIndex(searchIndexingIndex, "indexingStatusIndex",
				false);

		// file control collection

		fileControlCollection = gridFS.getDB().getCollection(
				CONT_FILE_COLLECTION);

		fileControlCollection.setWriteConcern(WriteConcern.FSYNC_SAFE);

		DBObject fileControlIndex = BasicDBObjectBuilder.start()
				.add(CONT_FILE_IDENTIFIER_KEY, 1).get();
		fileControlCollection.ensureIndex(fileControlIndex, "fileControlIndex",
				true);
	}

	@Override
	public Document retrieve(String identifier) throws DocStoreException {
		GridFSDBFile file = gridFS.findOne(identifier);
		if (file == null) {
			throw new DocumentNotFoundException(identifier);
		}

		return new MongoGridDocument(file);
	}

	private boolean controlFile(String identifier) {
		final DBObject controlRecord = BasicDBObjectBuilder.start(
				CONT_FILE_IDENTIFIER_KEY, identifier).get();
		final WriteResult wResult = fileControlCollection.insert(controlRecord);
		final CommandResult cResult = wResult.getCachedLastError();
		if (cResult.ok()) {
			return true;
		} else {
			System.err.println(cResult.getException());
			return false;
		}
	}

	private void releaseControlOfFile(String identifier) {
		final DBObject identifierQuery = new QueryBuilder()
				.and(CONT_FILE_IDENTIFIER_KEY).is(identifier).get();

		final WriteResult wResult = fileControlCollection
				.remove(identifierQuery);
		final CommandResult cResult = wResult.getCachedLastError();
		if (!cResult.ok()) {
			System.err.println(cResult.getException());
		}
	}

	private boolean fileExists(String identifier) {
		DBObject query = new BasicDBObject(Constants.FILENAME_KEY, identifier);
		DBObject result = filesCollection.findOne(query, RETRIEVE_OBJECT_ID);
		return result != null;
	}

	private void setFileMetaData(GridFSInputFile newFile, Document document) {
		newFile.setContentType(document.getMimeType());

		// store the encoding as meta-data for EncodedDocuments
		DBObject metaData = newFile.getMetaData();
		if (metaData == null) {
			metaData = new BasicDBObject();
		}

		metaData.put(INDEX_STATUS_KEY, STATUS_UNINDEXED);
		metaData.put(FILE_EXTENSION_KEY, document.getFileExtension());
		metaData.put(BINARY_ENCODING_KEY, document.getBinaryEncoding());

		newFile.setMetaData(metaData);
	}

	@Override
	protected boolean storeKnownName(Document document, String identifier,
			byte[] data, int length) {
		if (!controlFile(identifier)) {
			return true;
		}

		try {
			if (fileExists(identifier)) {
				return true;
			}

			GridFSInputFile newFile = gridFS
					.createFile(new ByteArrayInputStream(data, 0, length));
			newFile.setFilename(identifier);
			setFileMetaData(newFile, document);
			newFile.save();

			return false;
		} finally {
			releaseControlOfFile(identifier);
		}
	}

	private boolean setFileName(Object id, String newName) {
		DBObject query = new BasicDBObject("_id", id);
		DBObject update = new BasicDBObject("$set", new BasicDBObject(
				Constants.FILENAME_KEY, newName));
		DBObject fields = new BasicDBObject(Constants.FILENAME_KEY, 1);
		DBObject result = filesCollection.findAndModify(query, fields, null,
				false, update, false, false);
		if (result == null) {
			return false;
		} else {
			// TODO remove after initial debugging
			System.err.println((String) result.get(FILENAME_KEY)
					+ " renamed to " + newName);
			return true;
		}
	}

	/**
	 * Since we don't know the name, we'll have to save the data before we can
	 * determine the name. So save it under a random UUID, calculate the name,
	 * and if the name is not already in the file system then rename it.
	 * Otherwise delete it.
	 */
	@Override
	protected StoreRequestResult storeAndDetermineName(Document document,
			HashingInputStream inputStream) {
		final String temporaryName = java.util.UUID.randomUUID().toString();
		GridFSInputFile newFile = gridFS.createFile(inputStream);
		newFile.setFilename(temporaryName);
		setFileMetaData(newFile, document);
		newFile.save();

		final String actualName = inputStream.getDigest();

		if (!controlFile(actualName)) {
			gridFS.remove(temporaryName);
			return new StoreRequestResultImpl(actualName, true);
		}
		try {
			if (fileExists(actualName)) {
				gridFS.remove(temporaryName);
				return new StoreRequestResultImpl(actualName, true);
			} else {
				setFileName(newFile.getId(), actualName);
				return new StoreRequestResultImpl(actualName, false);
			}
		} finally {
			releaseControlOfFile(actualName);
		}
	}

	@Override
	public void markAsIndexed(String identifier) throws DocSearchException {
		final DBObject identifierQuery = new QueryBuilder().and(FILENAME_KEY)
				.is(identifier).get();

		final DBObject updateSet = BasicDBObjectBuilder.start()
				.add(INDEX_STATUS_QUERY, STATUS_INDEXED)
				.add(INDEX_TIMESTAMP_QUERY, new Date()).get();
		final BasicDBObject update = new BasicDBObject("$set", updateSet);

		final boolean doNotRemove = false;
		final boolean returnNewVersion = true;
		final boolean doNotUpsert = false;
		final DBObject doNotSort = null;

		DBObject result = filesCollection.findAndModify(identifierQuery,
				RETRIEVE_OBJECT_ID, doNotSort, doNotRemove, update,
				returnNewVersion, doNotUpsert);

		if (result == null) {
			throw new DocSearchException("could not mark document '"
					+ identifier + "' as indexed");
		}
	}

	@Override
	public String nextUnindexed() {
		final DBObject updateSet = BasicDBObjectBuilder.start()
				.add(INDEX_STATUS_QUERY, STATUS_INDEXING)
				.add(INDEX_TIMESTAMP_QUERY, new Date()).get();
		final BasicDBObject update = new BasicDBObject("$set", updateSet);

		final boolean doNotRemove = false;
		final boolean returnNewVersion = true;
		final boolean doNotUpsert = false;

		DBObject result = filesCollection.findAndModify(UNINDEXED_QUERY,
				RETRIEVE_OBJECT_ID_AND_FILENAME, SORT_BY_INDEX_TIMESTAMP,
				doNotRemove, update, returnNewVersion, doNotUpsert);

		if (result != null) {
			return (String) result.get(FILENAME_KEY);
		} else {
			return null;
		}
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
