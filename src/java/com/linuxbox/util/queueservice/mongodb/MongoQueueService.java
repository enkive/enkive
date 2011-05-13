package com.linuxbox.util.queueservice.mongodb;

import java.net.UnknownHostException;

import com.linuxbox.util.queueservice.QueueEntry;
import com.linuxbox.util.queueservice.QueueService;
import com.linuxbox.util.queueservice.QueueServiceException;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoQueueService implements QueueService {
	private Mongo mongo;
	private DB mongoDB;
	private DBCollection queueCollection;

	public MongoQueueService(String server, int port, String database,
			String collection) throws UnknownHostException, MongoException {
		mongo = new Mongo(server, port);
		mongoDB = mongo.getDB(database);
		queueCollection = mongoDB.getCollection(collection);
		finishConstruction();
	}

	public MongoQueueService(String database, String collection)
			throws UnknownHostException, MongoException {
		mongo = new Mongo();
		mongoDB = mongo.getDB(database);
		queueCollection = mongoDB.getCollection(collection);
		finishConstruction();
	}

	private void finishConstruction() {

	}

	@Override
	public void startup() throws QueueServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() throws QueueServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void enqueue(String identifier, int note)
			throws QueueServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public QueueEntry dequeue() throws QueueServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueueEntry dequeue(String identifer) throws QueueServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void finish(QueueEntry item) throws QueueServiceException {
		// TODO Auto-generated method stub

	}

}
