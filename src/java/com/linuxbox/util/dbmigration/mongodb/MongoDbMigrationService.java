package com.linuxbox.util.dbmigration.mongodb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.linuxbox.util.dbmigration.DbMigrationService;
import com.linuxbox.util.dbmigration.DbStatusRecord;
import com.linuxbox.util.dbmigration.DbStatusRecord.Status;
import com.linuxbox.util.dbmigration.DbVersionManager.DbVersion;
import com.linuxbox.util.mongodb.MongoIndexable;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

public class MongoDbMigrationService extends DbMigrationService implements
		MongoIndexable {
	protected final static String KEY_VERSION = "version";
	protected final static String KEY_STATUS = "status";
	protected final static String KEY_TIMESTAMP = "timestamp";

	protected final static DBObject QUERY_ALL_RECORDS;
	protected final static DBObject ALL_KEYS;
	protected final static DBObject ORDER_BY_VERSION_DESCENDING;

	protected DBCollection migrationsCollection;

	static {
		QUERY_ALL_RECORDS = new BasicDBObject();
		ALL_KEYS = BasicDBObjectBuilder.start().add(KEY_VERSION, 1)
				.add(KEY_STATUS, 1).add(KEY_TIMESTAMP, 1).get();
		ORDER_BY_VERSION_DESCENDING = BasicDBObjectBuilder.start()
				.add(KEY_VERSION, -1).get();
	}

	public MongoDbMigrationService(Mongo mongo, String dbName,
			String collectionName) {
		this(mongo.getDB(dbName).getCollection(collectionName));
	}

	public MongoDbMigrationService(MongoDbInfo dbInfo) {
		this(dbInfo.getCollection());
	}

	public MongoDbMigrationService(DBCollection migrationsCollection) {
		this.migrationsCollection = migrationsCollection;
		this.migrationsCollection.setWriteConcern(WriteConcern.FSYNC_SAFE);
	}

	@Override
	public DbStatusRecord getLatestDbStatusRecord() {
		DBObject result = migrationsCollection.findOne(QUERY_ALL_RECORDS,
				ALL_KEYS, ORDER_BY_VERSION_DESCENDING);

		if (null == result) {
			// if there is no record, create one with ordinal db version 0
			DbStatusRecord record = new DbStatusRecord(new DbVersion(0),
					DbStatusRecord.Status.STORED, new Date());
			DBObject mongoObj = dbStatusRecordToDbObject(record);
			migrationsCollection.save(mongoObj);
			return record;
		} else {
			return dbObjectToDBStatusRecord(result);
		}
	}

	/*
	 * Conversion methods
	 */

	protected static DBObject dbStatusRecordToDbObject(DbStatusRecord record) {
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		builder.add(KEY_VERSION, record.dbVersion);
		builder.add(KEY_STATUS, record.status.code);
		builder.add(KEY_TIMESTAMP, record.timestamp);
		return builder.get();
	}

	public static DbStatusRecord dbObjectToDBStatusRecord(DBObject dbObject) {
		final Integer version = (Integer) dbObject.get(KEY_VERSION);
		final Date timestamp = (Date) dbObject.get(KEY_TIMESTAMP);

		final Integer statusCode = (Integer) dbObject.get(KEY_STATUS);
		Status status;
		if (statusCode == Status.MIGRATING.code) {
			status = Status.MIGRATING;
		} else if (statusCode == Status.STORED.code) {
			status = Status.STORED;
		} else {
			status = Status.ERROR;
		}

		return new DbStatusRecord(new DbVersion(version), status, timestamp);
	}

	/*
	 * Methods implementing MongoIndexable
	 */

	@Override
	public List<DBObject> getIndexInfo() {
		return migrationsCollection.getIndexInfo();
	}

	@Override
	public List<IndexDescription> getPreferredIndexes() {
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add(
				KEY_VERSION, -1);
		IndexDescription byVersionDescending = new IndexDescription(
				"by version descending", builder.get(), true);
		List<IndexDescription> descriptionList = new ArrayList<IndexDescription>(
				1);
		descriptionList.add(byVersionDescending);
		return descriptionList;
	}

	@Override
	public void ensureIndex(DBObject index, DBObject options)
			throws MongoException {
		migrationsCollection.ensureIndex(index, options);
	}

	@Override
	public long getDocumentCount() throws MongoException {
		return migrationsCollection.find().count();
	}
}
