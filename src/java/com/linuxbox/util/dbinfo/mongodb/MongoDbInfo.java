package com.linuxbox.util.dbinfo.mongodb;

import com.linuxbox.util.dbinfo.AbstractDbInfo;
import com.linuxbox.util.dbinfo.DbInfo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MongoDbInfo extends AbstractDbInfo implements DbInfo {

	/*
	 * Perhaps we should just maintain the collection and only use that to
	 * return the MongoClient or the DB should they ever be queried?
	 */

	final MongoClient mongo;
	final DB database;
	final DBCollection collection;

	public MongoDbInfo(String serviceName, MongoClient mongo, String dbName,
			String collectionName) {
		this(serviceName, mongo, mongo.getDB(dbName), collectionName);
	}
	
	public MongoDbInfo(String serviceName, MongoClient mongo, DB db,
			String collectionName) {
		this(serviceName, mongo, db, db.getCollection(collectionName));
	}
	
	public MongoDbInfo(String serviceName, MongoClient mongo, DB db,
			DBCollection collection) {
		super(serviceName);
		this.mongo = mongo;
		this.database = db;
		this.collection = collection;
	}

	public MongoClient getMongo() {
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
