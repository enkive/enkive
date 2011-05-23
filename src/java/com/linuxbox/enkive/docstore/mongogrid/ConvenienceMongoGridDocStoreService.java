package com.linuxbox.enkive.docstore.mongogrid;

import java.net.UnknownHostException;

import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.util.lockservice.LockService;
import com.linuxbox.util.lockservice.LockServiceException;
import com.linuxbox.util.lockservice.mongodb.MongoLockService;
import com.linuxbox.util.queueservice.QueueService;
import com.linuxbox.util.queueservice.QueueServiceException;
import com.linuxbox.util.queueservice.mongodb.MongoQueueService;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Since making a MongoGridFSDocStoreService is somewhat complicated given that
 * it depends on other types of services, this is a convenience class that
 * subclasses MongoGridFSDocStoreService and creates those other services.
 * 
 * @author ivancich
 * 
 */
public class ConvenienceMongoGridDocStoreService extends
		MongoGridDocStoreService {
	private final static String DEFAULT_DOC_LOCK_NAME = "documentLockService";
	private final static String DEFAULT_INDEXER_QUEUE_NAME = "indexerQueueService";

	private Mongo mongo;
	private LockService myDocLockService;
	private QueueService myIndexerQueueService;
	private boolean createdMongo;

	String dbName;
	String bucketName;
	String docLockServiceCollectionName = DEFAULT_DOC_LOCK_NAME;
	String indexerQueueServiceCollectionName = DEFAULT_INDEXER_QUEUE_NAME;
	QueueService indexerQueueService;

	public ConvenienceMongoGridDocStoreService(String dbName, String bucketName)
			throws UnknownHostException, MongoException {
		this(new Mongo(), dbName, bucketName);
		createdMongo = true;
	}

	public ConvenienceMongoGridDocStoreService(Mongo mongo, String dbName,
			String bucketName) {
		super(mongo, dbName, bucketName);
		this.mongo = mongo;
		this.dbName = dbName;
		this.bucketName = bucketName;
	}

	public void startup() throws DocStoreException {
		try {
			myDocLockService = new MongoLockService(mongo, dbName,
					docLockServiceCollectionName);
			setDocumentLockingService(myDocLockService);
			myDocLockService.startup();

			myIndexerQueueService = new MongoQueueService(mongo, dbName,
					DEFAULT_INDEXER_QUEUE_NAME);
			setIndexerQueueService(myIndexerQueueService);
			myIndexerQueueService.startup();

			super.startup();
		} catch (Exception e) {
			throw new DocStoreException(
					"could not start ConvenienceMongoGridDocStoreService", e);
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

	public void setDocLockServiceCollectionName(
			String docLockServiceCollectionName) {
		this.docLockServiceCollectionName = docLockServiceCollectionName;
	}

	public void setIndexerQueueServiceCollectionName(
			String indexerQueueServiceCollectionName) {
		this.indexerQueueServiceCollectionName = indexerQueueServiceCollectionName;
	}
}
