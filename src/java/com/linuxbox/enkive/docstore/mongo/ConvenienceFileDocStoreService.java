/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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

import static com.linuxbox.enkive.docstore.mongogrid.Constants.INDEX_STATUS_KEY;

import java.net.UnknownHostException;

import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.util.lockservice.LockService;
import com.linuxbox.util.lockservice.LockServiceException;
import com.linuxbox.util.lockservice.mongodb.JavaLockService;
import com.linuxbox.util.queueservice.QueueService;
import com.linuxbox.util.queueservice.QueueServiceException;
import com.linuxbox.util.queueservice.mongodb.JavaQueueService;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

/**
 * Since making a FileDocStoreService is somewhat complicated given that
 * it depends on other types of services, this is a convenience class that
 * subclasses FileDocStoreService and creates those other services.
 * 
 * @author dang
 * 
 */
public class ConvenienceFileDocStoreService extends
		FileDocStoreService {
	private final static String DEFAULT_DOC_LOCK_NAME = "documentLockService";
	private final static String DEFAULT_INDEXER_QUEUE_NAME = "indexerQueueService";

	private MongoClient mongo;
	private LockService myDocLockService;
	private QueueService myIndexerQueueService;
	private boolean createdMongo;

	String docLockServiceCollectionName = DEFAULT_DOC_LOCK_NAME;
	String indexerQueueServiceCollectionName = DEFAULT_INDEXER_QUEUE_NAME;
	QueueService indexerQueueService;

	public ConvenienceFileDocStoreService(String basePath, String dbName, String collection)
			throws UnknownHostException, MongoException {
		this(basePath, new MongoClient(), dbName, collection);
		createdMongo = true;
	}

	public ConvenienceFileDocStoreService(String basePath, MongoClient mongo, String dbName,
			String collection) {
		this(basePath, mongo.getDB(dbName).getCollection(collection));
		this.mongo = mongo;
	}

	public ConvenienceFileDocStoreService(String basePath, DBCollection coll) {
		super(basePath, coll);
	}

	public void startup() throws DocStoreException {
		try {
			myDocLockService = new JavaLockService();
			setDocumentLockingService(myDocLockService);
			myDocLockService.startup();

			myIndexerQueueService = new JavaQueueService();
			setIndexerQueueService(myIndexerQueueService);
			((JavaQueueService)myIndexerQueueService).setDocStoreService(this);
			myIndexerQueueService.startup();

			super.startup();
		} catch (Exception e) {
			throw new DocStoreException(
					"could not start ConvenienceFileDocStoreService", e);
		}
	}

	public void shutdown() throws DocStoreException {
		DocStoreException lastException = null;

		try {
			myIndexerQueueService.shutdown();
		} catch (QueueServiceException e) {
			lastException = new DocStoreException(
					"could not shutdown indexer queue service", e);
		}

		try {
			myDocLockService.shutdown();
		} catch (LockServiceException e) {
			lastException = new DocStoreException(
					"could not shutdown document lock service", e);
		}

		if (createdMongo) {
			mongo.close();
		}

		if (lastException != null) {
			throw lastException;
		}
	}
	
	public int getStatus(String identifier) {
		DBObject dbo = lookupDocument(identifier);
		if (dbo == null) {
			return STATUS_UNKNOWN;
		}
		return (Integer)dbo.get(INDEX_STATUS_KEY);
	}

	public void setDocLockServiceCollectionName(
			String docLockServiceCollectionName) {
		this.docLockServiceCollectionName = docLockServiceCollectionName;
	}

	public void setIndexerQueueServiceCollectionName(
			String indexerQueueServiceCollectionName) {
		this.indexerQueueServiceCollectionName = indexerQueueServiceCollectionName;
	}
}
