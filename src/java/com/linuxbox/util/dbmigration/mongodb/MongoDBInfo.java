package com.linuxbox.util.dbmigration.mongodb;

import com.linuxbox.util.dbmigration.DBInfo;
import com.linuxbox.util.dbmigration.DBMigrationException;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

// NOAH: this might be incomplete; you might need something like the db name or collection name in order to create a secondary, temporary collection in which to place the new data

public class MongoDBInfo implements DBInfo {
	Mongo mongo;
	String dbName;
	DBCollection collection;

	public MongoDBInfo(Mongo mongo, String dbName, DBCollection collection) {
		this.mongo = mongo;
		this.dbName = dbName;
		this.collection = collection;
	}

	public Mongo getMongo() {
		return mongo;
	}

	public String getDbName() {
		return dbName;
	}

	public DBCollection getCollection() {
		return collection;
	}

	@Override
	public int getCurrentVersion() throws DBMigrationException {
		mongo.getDB(dbName).getCollection("migratorService");
		// TODO needs to call into the DB to get the current version number of the enkive services (return a set of all of them then
		//simply pick which one you need
		throw new DBMigrationException("unimplemented");
	}
}
