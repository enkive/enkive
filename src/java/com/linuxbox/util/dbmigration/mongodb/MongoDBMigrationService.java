package com.linuxbox.util.dbmigration.mongodb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.linuxbox.util.dbmigration.DBMigrationService;
import com.linuxbox.util.dbmigration.DBStatusRecord;
import com.linuxbox.util.dbmigration.DBStatusRecord.Status;
import com.linuxbox.util.mongodb.MongoIndexable;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

public class MongoDBMigrationService extends DBMigrationService implements
		MongoIndexable {
	protected final static String KEY_VERSION = "version";
	protected final static String KEY_STATUS = "status";
	protected final static String KEY_TIMESTAMP = "timestamp";

	protected final static DBObject QUERY_ALL_RECORDS;
	protected final static DBObject ALL_KEYS;
	protected final static DBObject ORDER_BY_VERSION_DESCENDING;

	protected Mongo mongo;
	protected String dbName;
	protected String collectionName;

	protected DBCollection dbCollection;

	static {
		QUERY_ALL_RECORDS = new BasicDBObject();
		ALL_KEYS = BasicDBObjectBuilder.start().add(KEY_VERSION, 1)
				.add(KEY_STATUS, 1).add(KEY_TIMESTAMP, 1).get();
		ORDER_BY_VERSION_DESCENDING = BasicDBObjectBuilder.start()
				.add(KEY_VERSION, -1).get();
	}

	public MongoDBMigrationService(Mongo mongo, String dbName,
			String collectionName) {
		this.mongo = mongo;
		this.dbName = dbName;
		this.collectionName = collectionName;

		final DB db = mongo.getDB(dbName);
		this.dbCollection = db.getCollection(collectionName);
		this.dbCollection.setWriteConcern(WriteConcern.FSYNC_SAFE);
	}

	@Override
	public DBStatusRecord getLatestDbStatusRecord() {
		DBObject result = dbCollection.findOne(QUERY_ALL_RECORDS, ALL_KEYS,
				ORDER_BY_VERSION_DESCENDING);

		if (null == result) {
			DBStatusRecord record = new DBStatusRecord(0,
					DBStatusRecord.Status.STORED, new Date());
			DBObject mongoObj = dBStatusRecordToDbObject(record);
			dbCollection.save(mongoObj);
			return record;
		} else {
			return DBObjectToDBStatusRecord(result);
		}
	}

	/*
	 * Conversion methods
	 */

	protected static DBObject dBStatusRecordToDbObject(DBStatusRecord record) {
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		builder.add(KEY_VERSION, record.version);
		builder.add(KEY_STATUS, record.status.code);
		builder.add(KEY_TIMESTAMP, record.timestamp);
		return builder.get();
	}

	public static DBStatusRecord DBObjectToDBStatusRecord(DBObject dbObject) {
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

		return new DBStatusRecord(version, status, timestamp);
	}

	/*
	 * Methods implementing MongoIndexable
	 */

	@Override
	public List<DBObject> getIndexInfo() {
		return dbCollection.getIndexInfo();
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
		dbCollection.ensureIndex(index, options);
	}

	@Override
	public long getDocumentCount() throws MongoException {
		return dbCollection.find().count();
	}
}
