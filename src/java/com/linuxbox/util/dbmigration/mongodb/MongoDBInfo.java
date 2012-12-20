package com.linuxbox.util.dbmigration.mongodb;

import static com.linuxbox.util.dbmigration.DBMigrationConstants.MIGRATOR_SERVICE_COLLECTION;
import static com.linuxbox.util.dbmigration.DBMigrationConstants.SERVICE;
import static com.linuxbox.util.dbmigration.DBMigrationConstants.VERSION;

import com.linuxbox.util.dbmigration.DBInfo;
import com.linuxbox.util.dbmigration.DBMigrationException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoDBInfo implements DBInfo {
	Mongo mongo;
	String dbName;
	String serviceName;
	DBCollection collection;
	protected static DBCollection migratorColl = null;

	public MongoDBInfo(Mongo mongo, String dbName, String collectionName,
			String serviceName) {
		this.mongo = mongo;
		this.dbName = dbName;
		this.collection = mongo.getDB(dbName).getCollection(collectionName);
		this.serviceName = serviceName;

		if (mongo.getDB(dbName).collectionExists(MIGRATOR_SERVICE_COLLECTION)) {
			migratorColl = mongo.getDB(dbName).getCollection(
					MIGRATOR_SERVICE_COLLECTION);
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
		if (migratorColl != null) {
			DBObject serviceVersionData = migratorColl
					.findOne(new BasicDBObject(SERVICE, serviceName));
			Integer version = null;

			if (serviceVersionData != null) {
				version = (Integer) serviceVersionData.get(VERSION);
			}

			if (version != null) {
				return version;
			} else {
				throw new DBMigrationException("The current version for the "
						+ serviceName + " could not be found.");
			}
		} else {
			return 1;
		}
	}
}
