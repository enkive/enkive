package com.linuxbox.util.dbinfo.mongodb;

import com.linuxbox.util.dbinfo.AbstractDBInfo;
import com.linuxbox.util.dbinfo.DBInfo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoDBInfo extends AbstractDBInfo implements DBInfo {

	/*
	 * Perhaps we should just maintain the collection and only use that to
	 * return the Mongo or the DB should they ever be queried?
	 */

	final Mongo mongo;
	final DB database;
	final DBCollection collection;

	public MongoDBInfo(String serviceName, Mongo mongo, String dbName,
			String collectionName) {
		this(serviceName, mongo, mongo.getDB(dbName), collectionName);
	}
	
	public MongoDBInfo(String serviceName, Mongo mongo, DB db,
			String collectionName) {
		this(serviceName, mongo, db, db.getCollection(collectionName));
	}
	
	public MongoDBInfo(String serviceName, Mongo mongo, DB db,
			DBCollection collection) {
		super(serviceName);
		this.mongo = mongo;
		this.database = db;
		this.collection = collection;
	}

	public Mongo getMongo() {
		return mongo;
	}

	public DB getDatabase() {
		return database;
	}

	public DBCollection getCollection() {
		return collection;
	}
	
	public String getDbName() {
		return database.getName();
	}

	public String getCollectionName() {
		return collection.getName();
	}
}
