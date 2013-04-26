package com.linuxbox.util.dbinfo.mongodb;

import com.linuxbox.util.dbinfo.AbstractDBInfo;
import com.linuxbox.util.dbinfo.DBInfo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoDBInfo extends AbstractDBInfo implements DBInfo {
	final Mongo mongo;
	final String dbName;
	final String collectionName;
	final DB database;
	final DBCollection collection;

	public MongoDBInfo(Mongo mongo, String dbName, String collectionName,
			String serviceName) {
		super(serviceName);
		this.mongo = mongo;
		this.dbName = dbName;
		this.collectionName = collectionName;
		
		this.database = mongo.getDB(dbName);
		this.collection = database.getCollection(collectionName);
	}

	public Mongo getMongo() {
		return mongo;
	}

	public String getDbName() {
		return dbName;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public DB getDatabase() {
		return database;
	}

	public DBCollection getCollection() {
		return collection;
	}
}
