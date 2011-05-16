package com.linuxbox.enkive.docstore.mongogrid;

import static com.linuxbox.enkive.docstore.mongogrid.Constants.BINARY_ENCODING_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_EXTENSION_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_SHARD_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_SHARD_QUERY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_STATUS_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_STATUS_QUERY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_TIMESTAMP_KEY;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_TIMESTAMP_QUERY;
import static com.linuxbox.util.mongodb.MongoDBConstants.FILENAME_KEY;
import static com.linuxbox.util.mongodb.MongoDBConstants.GRID_FS_FILES_COLLECTION_SUFFIX;
import static com.linuxbox.util.mongodb.MongoDBConstants.OBJECT_ID_KEY;

import java.io.ByteArrayInputStream;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.AbstractDocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.StoreRequestResult;
import com.linuxbox.enkive.docstore.StoreRequestResultImpl;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentNotFoundException;
import com.linuxbox.util.HashingInputStream;
import com.linuxbox.util.lockservice.LockService;
import com.linuxbox.util.lockservice.LockServiceException;
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

/*
 * NOTE: we may have some challenges w.r.t. atomicity using MongoDB for a filestore.
 * Please see:
 * http://groups.google.com/group/mongodb-user/browse_thread/thread/4a7419e07c73537/2931a3163836d6ba
 */

public class MongoGridDocStoreService extends AbstractDocStoreService {
	@SuppressWarnings("unused")
	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.docstore.mongogrid");

	/*
	 * The various status value a document can have to indicate its state of
	 * indexing.
	 */
	static final int STATUS_UNINDEXED = 0;
	static final int STATUS_INDEXING = 1;
	static final int STATUS_INDEXED = 2;
	static final int STATUS_ERROR = 3;
	static final int STATUS_STALE = 4;

	/*
	 * Notations for lock records.
	 */
	private static final String LOCK_CREATE_NOTE = "create";
	private static final String LOCK_REMOVE_NOTE = "remove";

	final static DBObject SORT_BY_INDEX_TIMESTAMP = new BasicDBObject(
			INDEX_TIMESTAMP_QUERY, 1);
	final static DBObject UNINDEXED_QUERY = new QueryBuilder()
			.and(INDEX_STATUS_QUERY).is(STATUS_UNINDEXED).get();
	final static DBObject RETRIEVE_OBJECT_ID = BasicDBObjectBuilder.start()
			.add(OBJECT_ID_KEY, 1).get();
	final static DBObject RETRIEVE_OBJECT_ID_AND_FILENAME = BasicDBObjectBuilder
			.start().add(OBJECT_ID_KEY, 1).add(FILENAME_KEY, 1).get();

	GridFS gridFS; // keep visible to tests
	private Mongo mongo;
	private DBCollection filesCollection;
	private LockService docLockService;
	private boolean createdMongo;

	public MongoGridDocStoreService(String host, int port, String dbName,
			String bucketName) throws UnknownHostException {
		this(new Mongo(host, port), dbName, bucketName);
		createdMongo = true;
	}

	public MongoGridDocStoreService(String dbName, String bucketName)
			throws UnknownHostException {
		this(new Mongo(), dbName, bucketName);
		createdMongo = true;
	}

	public MongoGridDocStoreService(Mongo mongo, String dbName,
			String bucketName) {
		this.mongo = mongo;

		DB db = mongo.getDB(dbName);
		gridFS = new GridFS(db, bucketName);

		// files collection

		filesCollection = gridFS.getDB().getCollection(
				bucketName + GRID_FS_FILES_COLLECTION_SUFFIX);

		/*
		 * NOTE: we DO NOT NEED a filename index, because by default GridFS will
		 * create an index on filename and upload date. You can efficiently
		 * query on compound indexes if the key searched for come before those
		 * that are not, which is true in this case.
		 * 
		 * DBObject filenameIndex = BasicDBObjectBuilder.start()
		 * .add(FILENAME_KEY, 1).get();
		 * filesCollection.ensureIndex(filenameIndex);
		 */

		// be sure to put status before timestamp, because it's more likely
		// we'll search on just status rather than on just timestamp
		DBObject searchIndexingIndex = BasicDBObjectBuilder.start()
				.add(INDEX_STATUS_KEY, 1).add(INDEX_TIMESTAMP_KEY, 1).get();
		filesCollection.ensureIndex(searchIndexingIndex, "indexingStatusIndex",
				false);
	}

	@Override
	public void subStartup() throws DocStoreException {
		if (docLockService == null) {
			throw new DocStoreException("document lock service not set");
		}
	}

	@Override
	public void subShutdown() {
		if (createdMongo) {
			mongo.close();
		}
	}

	/**
	 * Retrieves a document from the document store.
	 */
	@Override
	public Document retrieve(String identifier) throws DocStoreException {
		GridFSDBFile file = gridFS.findOne(identifier);
		if (file == null) {
			throw new DocumentNotFoundException(identifier);
		}

		return new MongoGridDocument(file);
	}

	@Override
	protected StoreRequestResult storeKnownHash(Document document, byte[] hash,
			byte[] data, int length) throws DocStoreException {
		try {
			final String identifier = getFileNameFromHash(hash);
			final int shardKey = getShardIndexFromHash(hash);

			if (!docLockService.lock(identifier, LOCK_CREATE_NOTE)) {
				// TODO we should note whether the controller is creating or
				// removing; if creating we're done; if removing we should
				// re-create
				return new StoreRequestResultImpl(identifier, true);
			}

			try {
				if (fileExists(identifier)) {
					return new StoreRequestResultImpl(identifier, true);
				}

				GridFSInputFile newFile = gridFS
						.createFile(new ByteArrayInputStream(data, 0, length));
				newFile.setFilename(identifier);
				setFileMetaData(newFile, document, shardKey);
				newFile.save();

				// TODO: is there anything we should do at this point to insure
				// that
				// it's actually stored?

				return new StoreRequestResultImpl(identifier, false);
			} finally {
				docLockService.releaseLock(identifier);
			}
		} catch (Exception e) {
			throw new DocStoreException(e);
		}
	}

	/**
	 * Since we don't know the name, we'll have to save the data before we can
	 * determine the name. So save it under a random UUID, calculate the name,
	 * and if the name is not already in the file system then rename it.
	 * Otherwise delete it.
	 * 
	 * @throws DocSearchException
	 */
	@Override
	protected StoreRequestResult storeAndDetermineHash(Document document,
			HashingInputStream inputStream) throws DocStoreException {
		final String temporaryName = java.util.UUID.randomUUID().toString();
		GridFSInputFile newFile = gridFS.createFile(inputStream);
		newFile.setFilename(temporaryName);
		setFileMetaData(newFile, document, -1);
		newFile.save();

		// TODO: is there anything we should do at this point to insure that
		// it's actually stored?

		final byte[] actualHash = inputStream.getDigest();
		final String actualName = getFileNameFromHash(actualHash);
		final int shardKey = getShardIndexFromHash(actualHash);

		try {
			if (!docLockService.lock(actualName, LOCK_CREATE_NOTE)) {
				gridFS.remove(temporaryName);

				// TODO: is there anything we should do at this point to insure
				// that
				// it's actually been removed?

				return new StoreRequestResultImpl(actualName, true);
			}

			// so now we're in "control" of that file

			try {
				// so now we're in "control" of that file

				if (fileExists(actualName)) {
					gridFS.remove(temporaryName);
					return new StoreRequestResultImpl(actualName, true);
				} else {
					final boolean wasRenamed = setFileNameAndShardKey(
							newFile.getId(), actualName, shardKey);
					if (!wasRenamed) {
						throw new DocStoreException(
								"expected to find and rename a GridFS file with id \""
										+ newFile.getId()
										+ "\" but could not find it");
					}
					return new StoreRequestResultImpl(actualName, false);
				}
			} finally {
				docLockService.releaseLock(actualName);
			}
		} catch (LockServiceException e) {
			throw new DocStoreException(e);
		}
	}

	@Override
	public void markAsIndexed(String identifier) throws DocStoreException {
		markAsIndexedHelper(identifier, STATUS_INDEXED);
	}

	@Override
	public void markAsErrorIndexing(String identifier) throws DocStoreException {
		markAsIndexedHelper(identifier, STATUS_ERROR);
	}

	@Override
	protected String nextUnindexedByShardKey(int shardKeyLow, int shardKeyHigh) {
		final DBObject query = createUnindexedQuery(shardKeyLow, shardKeyHigh);
		final DBObject updateSet = BasicDBObjectBuilder.start()
				.add(INDEX_STATUS_QUERY, STATUS_INDEXING)
				.add(INDEX_TIMESTAMP_QUERY, new Date()).get();
		final BasicDBObject update = new BasicDBObject("$set", updateSet);

		// some constants to make the call to findAndModify more readable;
		// please Ms. Compiler, optimize them away!
		final boolean doNotRemove = false;
		final boolean returnNewVersion = true;
		final boolean doNotUpsert = false;

		DBObject result = filesCollection.findAndModify(query,
				RETRIEVE_OBJECT_ID_AND_FILENAME, SORT_BY_INDEX_TIMESTAMP,
				doNotRemove, update, returnNewVersion, doNotUpsert);

		if (result != null) {
			return (String) result.get(FILENAME_KEY);
		} else {
			return null;
		}
	}

	@Override
	public boolean remove(String identifier) throws DocStoreException {
		try {
			if (!docLockService.lock(identifier, LOCK_REMOVE_NOTE)) {
				// TODO if we're here and someone else was trying to delete or
				// create this, then we should do nothing; the file either
				// needed to
				// be recreated, or it was already removed; let's lie and say we
				// succeeded!
				return true;
			}

			// in control of file

			try {
				// in control of file

				if (fileExists(identifier)) {
					gridFS.remove(identifier);
					return true;
				} else {
					return false;
				}
			} finally {
				docLockService.releaseLock(identifier);
			}
		} catch (LockServiceException e) {
			throw new DocStoreException(e);
		}
	}

	/*
	 * SUPPORT METHODS
	 */

	DB getDb() {
		return gridFS.getDB();
	}

	public void setDocumentLockService(LockService lockService) {
		this.docLockService = lockService;
	}

	/**
	 * Simple test as to whether a file exists. There better bean index on the
	 * filename!
	 * 
	 * @param identifier
	 * @return
	 */
	boolean fileExists(String identifier) {
		DBObject query = new BasicDBObject(Constants.FILENAME_KEY, identifier);
		DBObject result = filesCollection.findOne(query, RETRIEVE_OBJECT_ID);
		return result != null;
	}

	void setFileMetaData(GridFSInputFile newFile, Document document,
			int shardKey) {
		newFile.setContentType(document.getMimeType());

		// store the encoding as meta-data for EncodedDocuments
		DBObject metaData = newFile.getMetaData();
		if (metaData == null) {
			metaData = new BasicDBObject();
		}

		metaData.put(INDEX_STATUS_KEY, STATUS_UNINDEXED);
		metaData.put(FILE_EXTENSION_KEY, document.getFileExtension());
		metaData.put(BINARY_ENCODING_KEY, document.getBinaryEncoding());
		metaData.put(INDEX_SHARD_KEY, shardKey);

		newFile.setMetaData(metaData);
	}

	/**
	 * Change the file name of GridFS file with a given id to newName.
	 * 
	 * @param id
	 * @param newName
	 * @return true if a file was modified, false otherwise
	 */
	boolean setFileNameAndShardKey(Object id, String newName, int shardKey) {
		DBObject query = new BasicDBObject(OBJECT_ID_KEY, id);
		DBObject updateItems = BasicDBObjectBuilder.start()
				.add(Constants.FILENAME_KEY, newName)
				.add(Constants.INDEX_SHARD_QUERY, shardKey).get();
		DBObject update = new BasicDBObject("$set", updateItems);
		DBObject fields = new BasicDBObject(Constants.FILENAME_KEY, 1);
		final boolean returnOld = false;
		DBObject result = filesCollection.findAndModify(query, fields, null,
				false, update, returnOld, false);
		return result != null;
	}

	void markAsIndexedHelper(String identifier, int status)
			throws DocStoreException {
		final DBObject identifierQuery = new QueryBuilder().and(FILENAME_KEY)
				.is(identifier).get();

		final DBObject updateSet = BasicDBObjectBuilder.start()
				.add(INDEX_STATUS_QUERY, status)
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
			throw new DocStoreException("could not mark document '"
					+ identifier + "' with indexing status of " + status);
		}
	}

	private DBObject createUnindexedQuery(int shardKeyLow, int shardKeyHigh) {
		QueryBuilder protoQuery = new QueryBuilder();

		protoQuery.and(INDEX_STATUS_QUERY).is(STATUS_UNINDEXED);

		if (shardKeyLow > 0) {
			protoQuery.and(INDEX_SHARD_QUERY).greaterThanEquals(shardKeyLow);
		}

		if (shardKeyHigh < INDEX_SHARD_KEY_COUNT) {
			protoQuery.and(INDEX_SHARD_QUERY).lessThan(shardKeyHigh);
		}

		final DBObject finalQuery = protoQuery.get();
		return finalQuery;
	}
}
