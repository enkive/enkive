package com.linuxbox.util.dbmigration.mongodb;

import com.linuxbox.util.dbmigration.DBInfo;
import com.linuxbox.util.dbmigration.DBMigrationException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoDBInfo implements DBInfo {
	Mongo mongo;
	String dbName;
	String serviceName;
	DBCollection collection;
	protected static DBCollection migratorColl = null;
	
	public MongoDBInfo(Mongo mongo, String dbName, String collectionName, String serviceName) {
		this.mongo = mongo;
		this.dbName = dbName;
		this.collection = mongo.getDB(dbName).getCollection(collectionName);
		
		if(mongo.getDB(dbName).collectionExists("migratorService")) {
			migratorColl = mongo.getDB(dbName).getCollection("migratorService");
		}
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
		if(migratorColl != null) {
			Integer version = (Integer)(migratorColl.findOne(new BasicDBObject("service", serviceName)).get("version"));
			if(version != null){
				return version;
			} else {
				throw new DBMigrationException("The version for the " + serviceName + " could not be found.");
			}
		} else {
			return 1;
		}
	}
}
