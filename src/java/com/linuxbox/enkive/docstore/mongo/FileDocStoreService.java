/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
 *
 * This file is part of Enkive CE (Community Edition).
 *
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.linuxbox.enkive.docstore.mongo;

import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_CDATE;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_ENCODING;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_EXTENSION;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_ID;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_NAME;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_PATH;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_SIZE;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.FILE_TYPE;
import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_STATUS_KEY;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.AbstractDocStoreService;
import com.linuxbox.enkive.docstore.DocStoreConstants;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.FileSystemDocument;
import com.linuxbox.enkive.docstore.StoreRequestResult;
import com.linuxbox.enkive.docstore.StoreRequestResultImpl;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentNotFoundException;
import com.linuxbox.util.DirectoryManagement;
import com.linuxbox.util.HashingInputStream;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.linuxbox.util.lockservice.LockAcquisitionException;
import com.linuxbox.util.lockservice.LockService;
import com.linuxbox.util.lockservice.LockServiceException;
import com.linuxbox.util.mongodb.MongoIndexable;
import com.linuxbox.util.mongodb.UpdateFieldBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

/*
 * Docstore service that stores in files in the FS.
 */

public class FileDocStoreService extends AbstractDocStoreService implements
		MongoIndexable {
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.docstore.filestore");

	/*
	 * These are used to obtain the locks.
	 */
	private final static int LOCK_RETRIES = 10;
	private final static long LOCK_RETRY_DELAY_MILLISECONDS = 10000;

	/*
	 * The various status value a document can have to indicate its state of
	 * indexing.
	 */
	static final int STATUS_UNINDEXED = 0;
	static final int STATUS_INDEXING = 1;
	static final int STATUS_INDEXED = 2;
	static final int STATUS_ERROR = 3;
	static final int STATUS_STALE = 4;
	static final int STATUS_UNKNOWN = -1;

	final static DBObject UNINDEXED_QUERY = new QueryBuilder()
			.and(INDEX_STATUS_KEY).is(STATUS_UNINDEXED).get();

	protected DBCollection filesColl;
	private String basePath;
	private LockService documentLockingService;
	private File tempDir;

	public FileDocStoreService(String basePath, MongoDbInfo dbInfo) {
		this(basePath, dbInfo.getCollection());
	}

	public FileDocStoreService(String basePath, DBCollection collection) {
		super();
		this.basePath = basePath;
		this.filesColl = collection;
	}

	@Override
	public void subStartup() throws DocStoreException {
		try {
			File f = new File(basePath);
			basePath = f.getCanonicalPath();
			DirectoryManagement.verifyDirectory(basePath, "DocStore file storage directory");
			this.tempDir = new File(basePath + "/temp");
			DirectoryManagement.verifyDirectory(tempDir, "DocStore temporary file storage directory");
		} catch (IOException e) {
			throw new DocStoreException(e);
		}
	}

	@Override
	public void subShutdown() {
	}

	/**
	 * Retrieves a document from the document store.
	 */
	@Override
	public Document retrieve(String identifier) throws DocStoreException,
			DocumentNotFoundException {
		DBObject dbo = lookupDocument(identifier);
		if (dbo == null) {
			throw new DocumentNotFoundException(identifier);
		}
		File f = new File((String) dbo.get(FILE_PATH));
		if (!f.isFile()) {
			throw new DocumentNotFoundException(identifier);
		}
		String mimeType = (String)dbo.get(FILE_TYPE);
		String extension = (String)dbo.get(FILE_EXTENSION);
		String name = (String)dbo.get(FILE_NAME);
		String encoding = (String)dbo.get(FILE_ENCODING);
		
		return new FileSystemDocument(f.getAbsolutePath(), mimeType, name, extension, encoding);
	}

	@Override
	protected StoreRequestResult storeKnownHash(Document document, byte[] hash,
			byte[] data, int length) throws DocStoreException {
		try {
			final String identifier = getIdentifierFromHash(hash);
			
			try {
				documentLockingService.lockWithRetries(identifier,
						DocStoreConstants.LOCK_TO_STORE, LOCK_RETRIES,
						LOCK_RETRY_DELAY_MILLISECONDS);
			} catch (LockAcquisitionException e) {
				throw new DocStoreException(
						"could not acquire lock to store document \""
								+ identifier + "\"");
			}
			
			File f = null;
			try {
				if (lookupDocument(identifier) != null) {
					return new StoreRequestResultImpl(identifier, true, 1);
				}
				String path = idToPath(identifier);

				f = new File(path);
				f.getParentFile().mkdirs();
				FileOutputStream os = new FileOutputStream(f);
				os.write(data, 0, length);
				os.close();

				BasicDBObject dbo = new BasicDBObject();
				dbo.put(FILE_ID, identifier);
				dbo.put(FILE_PATH, path);
				dbo.put(FILE_SIZE, length);
				dbo.put(FILE_CDATE, new Date());
				dbo.put(FILE_TYPE, document.getMimeType());
				dbo.put(FILE_NAME, document.getFilename());
				dbo.put(FILE_EXTENSION, document.getFileExtension());
				dbo.put(FILE_ENCODING, document.getBinaryEncoding());
				dbo.put(INDEX_STATUS_KEY, STATUS_UNINDEXED);
				filesColl.insert(dbo);

				return new StoreRequestResultImpl(identifier, false, 1);
			} catch (IOException e) {
				throw new DocStoreException("Failed to store file", e);
			} catch (MongoException e) {
				f.delete();
				throw new DocStoreException("Failed to store file", e);
			} finally {
				documentLockingService.releaseLock(identifier);
			}
			
		} catch (Exception e) {
			LOGGER.error("Could not save document to filesystem", e);
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
		
		File tempFile;
		long length;
		try {
			tempFile = File.createTempFile("enkive", "txt", tempDir);
			FileOutputStream os = new FileOutputStream(tempFile);
			IOUtils.copy(inputStream, os);
			length = os.getChannel().position();
		} catch (IOException e) {
			throw new DocStoreException("Failed to store file", e);
		}

		final byte[] actualHash = inputStream.getDigest();
		final String actualName = getIdentifierFromHash(actualHash);

		try {
			try {
				documentLockingService.lockWithRetries(actualName,
						DocStoreConstants.LOCK_TO_STORE, LOCK_RETRIES,
						LOCK_RETRY_DELAY_MILLISECONDS);
			} catch (LockAcquisitionException e) {
				tempFile.delete();
				throw new DocStoreException(
						"could not acquire lock to store document \""
								+ actualName + "\"");
			}

			// so now we're in "control" of that file

			File f = null;
			try {
				if (fileExists(actualName)) {
					tempFile.delete();
					return new StoreRequestResultImpl(actualName, true, 1);
				} else {
					String path = idToPath(actualName);
					f = new File(path);
					f.getParentFile().mkdirs();
					final boolean wasRenamed = tempFile.renameTo(f);
					if (!wasRenamed) {
						throw new DocStoreException(
								"expected to find and rename a file with id \""
										+ actualName
										+ "\" but could not find it");
					}

					BasicDBObject dbo = new BasicDBObject();
					dbo.put(FILE_ID, actualName);
					dbo.put(FILE_PATH, path);
					dbo.put(FILE_SIZE, (int)length);
					dbo.put(FILE_CDATE, new Date());
					dbo.put(FILE_TYPE, document.getMimeType());
					dbo.put(FILE_NAME, document.getFilename());
					dbo.put(FILE_EXTENSION, document.getFileExtension());
					dbo.put(FILE_ENCODING, document.getBinaryEncoding());
					dbo.put(INDEX_STATUS_KEY, STATUS_UNINDEXED);
					filesColl.insert(dbo);

					return new StoreRequestResultImpl(actualName, false, 1);
				}
			} catch (MongoException e) {
				f.delete();
				throw new DocStoreException("Failed to store file", e);
			} finally {
				documentLockingService.releaseLock(actualName);
			}
		} catch (LockServiceException e) {
			throw new DocStoreException(e);
		}
	}

	@Override
	public boolean isIndexed(String identifier) throws DocStoreException, DocumentNotFoundException {
		DBObject dbo = lookupDocument(identifier);
		if (dbo == null) {
			throw new DocumentNotFoundException(identifier);
		}
		return ((Integer) dbo.get(INDEX_STATUS_KEY) == STATUS_INDEXED);
	}

	@Override
	public void markAsIndexed(String identifier) throws DocStoreException {
		statusHelper(identifier, STATUS_INDEXED);
	}

	@Override
	public void markAsErrorIndexing(String identifier) throws DocStoreException {
		statusHelper(identifier, STATUS_ERROR);
	}

	@Override
	protected String nextUnindexedByShardKey(int shardKeyLow, int shardKeyHigh) {
		final DBObject updateSet = BasicDBObjectBuilder.start()
				.add(INDEX_STATUS_KEY, STATUS_INDEXING).get();
		final BasicDBObject update = new BasicDBObject("$set", updateSet);
		DBObject result = filesColl.findAndModify(UNINDEXED_QUERY, update);

		if (result != null) {
			return result.get(FILE_ID).toString();
		}

		return null;
	}

	@Override
	public boolean remove(String identifier) throws DocStoreException {
		try {
			try {
				documentLockingService.lock(identifier,
						DocStoreConstants.LOCK_TO_REMOVE);
			} catch (LockAcquisitionException e) {
				// TODO VERIFY: if we're here and someone else was trying to
				// delete or create this, then we should do nothing; the file
				// either needed to be recreated, or it was already removed;
				// let's lie and say we succeeded!
				return true;
			}

			// in control of file

			try {
				// in control of file

				DBObject dbo = lookupDocument(identifier);
				if (dbo == null) {
					return false;
				}
				new File(dbo.get(FILE_PATH).toString()).delete();
				filesColl.remove(dbo);
				return true;
			} finally {
				documentLockingService.releaseLock(identifier);
			}
		} catch (LockServiceException e) {
			throw new DocStoreException(e);
		}
	}

	/*
	 * SUPPORT METHODS
	 */

	public LockService getDocumentLockingService() {
		return this.documentLockingService;
	}

	public void setDocumentLockingService(LockService lockService) {
		this.documentLockingService = lockService;
	}

	/**
	 * Simple test as to whether a file exists. There better be an index on the
	 * filename!
	 * 
	 * @param identifier
	 * @return
	 */
	boolean fileExists(String identifier) {
		if (lookupDocument(identifier) == null) {
			return false;
		}
		File f = new File(idToPath(identifier));
		return f.isFile();
	}

	protected DBObject lookupDocument(String identifier) {
		return filesColl.findOne(identifier);
	}

	private void statusHelper(String identifier, int status) throws DocStoreException {
		DBObject dbo = lookupDocument(identifier);
		if (dbo == null) {
			throw new DocStoreException("Cannot set index status for " + identifier +
					". It is not in docstore");
		}

		DBObject update = new UpdateFieldBuilder().set(INDEX_STATUS_KEY, status).get();
		try {
			filesColl.update(dbo, update);
		} catch (MongoException e) {
			throw new DocStoreException("Failed to set index status for " + identifier, e);
		}
	}

	private String idToPath(String identifier) {
		String path = this.basePath;
		
		path += "/";
		path += identifier.substring(0, 2);
		path += "/";
		path += identifier.substring(2, 4);
		path += "/";
		path += identifier.substring(4, 6);
		path += "/";
		path += identifier.substring(6, 8);
		path += "/";
		path += identifier.substring(8);
		return path;
	}

	@Override
	public List<DBObject> getIndexInfo() {
		return filesColl.getIndexInfo();
	}

	@Override
	public List<IndexDescription> getPreferredIndexes() {
		List<IndexDescription> result = new LinkedList<IndexDescription>();
		// We don't need an index for FILE_ID, since that is stored in _id which
		// is always indexed

		DBObject indexedIndex = BasicDBObjectBuilder.start().add(INDEX_STATUS_KEY, 1).get();
		IndexDescription id = new IndexDescription(INDEX_STATUS_KEY, indexedIndex, false);
		result.add(id);

		return result;
	}

	@Override
	public void ensureIndex(DBObject index, DBObject options)
			throws MongoException {
		filesColl.ensureIndex(index, options);
	}

	@Override
	public long getDocumentCount() throws MongoException {
		return filesColl.getCount();
	}
}
