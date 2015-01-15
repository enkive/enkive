package com.linuxbox.util.dbmigration.mongodb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.linuxbox.enkive.ProductInfo;
import com.linuxbox.enkive.tool.mongodb.MongoDbIndexManager;
import com.linuxbox.util.Version;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.linuxbox.util.dbmigration.DbMigrationService;
import com.linuxbox.util.dbmigration.DbStatusRecord;
import com.linuxbox.util.dbmigration.DbStatusRecord.Status;
import com.linuxbox.util.dbmigration.DbVersionManager.DbVersion;
import com.linuxbox.util.dbmigration.DbVersionManager.DbVersionManagerException;
import com.linuxbox.util.mongodb.MongoIndexable;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
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
	protected final static DBObject ORDER_BY_TIMESTAMP_VERSION_STATUS_DESCENDING;

	protected DBCollection migrationsCollection;

	static {
		QUERY_ALL_RECORDS = new BasicDBObject();
		ALL_KEYS = BasicDBObjectBuilder.start().add(KEY_VERSION, 1)
				.add(KEY_STATUS, 1).add(KEY_TIMESTAMP, 1).get();
		ORDER_BY_VERSION_DESCENDING = BasicDBObjectBuilder.start()
				.add(KEY_VERSION, -1).get();
		ORDER_BY_TIMESTAMP_VERSION_STATUS_DESCENDING = BasicDBObjectBuilder
				.start().add(KEY_TIMESTAMP, -1).add(KEY_VERSION, -1)
				.add(KEY_STATUS, -1).get();

	}

	public MongoDbMigrationService(MongoClient mongo, String dbName,
			String collectionName) {
		this(mongo.getDB(dbName).getCollection(collectionName));
	}

	public MongoDbMigrationService(MongoDbInfo dbInfo) {
		this(dbInfo.getCollection());
	}

	public MongoDbMigrationService(DBCollection migrationsCollection) {
		this.migrationsCollection = migrationsCollection;
		this.migrationsCollection.setWriteConcern(WriteConcern.FSYNC_SAFE);

		MongoDbIndexManager m = new MongoDbIndexManager();
		m.forceRequestedIndexes("MongoDbMigrationService", this);
	}

	@Override
	public DbStatusRecord getLatestDbStatusRecord(boolean successful)
			throws DbVersionManagerException {
		if (isDbNew()) {
			// if the db is new, we can assume it will be created according
			// to current version, so store current version
			Version softwareVersion = ProductInfo.VERSION;
			DbVersion databaseVersion = dbVersionManager
					.appropriateDbVersionFor(softwareVersion);
			DbStatusRecord record = new DbStatusRecord(new DbVersion(
					databaseVersion.ordinal), DbStatusRecord.Status.STORED,
					new Date());
			addDbStatusRecord(record);
			return record;
		}

		DBCursor cursor = migrationsCollection.find().sort(ORDER_BY_TIMESTAMP_VERSION_STATUS_DESCENDING);

		try {
			DbStatusRecord record;
			while (cursor.hasNext()) {
				record = dbObjectToDBStatusRecord(cursor.next());
				if (!successful) {
					return record;
				} else if (record.status.code == Status.STORED.code) {
					return record;
				}
			}
			// if there is no record of the required type, create one with ordinal db version 0
			record = new DbStatusRecord(new DbVersion(0), DbStatusRecord.Status.STORED, new Date());
			addDbStatusRecord(record);
			return record;
		} finally {
			cursor.close();
		}
	}

	@Override
	public void addDbStatusRecord(DbStatusRecord record) {
		DBObject statusObject = dbStatusRecordToDbObject(record);
		migrationsCollection.save(statusObject);
	}

	/*
	 * Conversion methods
	 */

	protected static DBObject dbStatusRecordToDbObject(DbStatusRecord record) {
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		builder.add(KEY_VERSION, record.dbVersion.ordinal);
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
		IndexDescription byTimestampVersionStatusDescending = new IndexDescription(
				"by timestamp, version, status descending",
				ORDER_BY_TIMESTAMP_VERSION_STATUS_DESCENDING, true);
		List<IndexDescription> descriptionList = new ArrayList<IndexDescription>(
				1);
		descriptionList.add(byTimestampVersionStatusDescending);
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

	public boolean isDbNew() {
		final DB database = migrationsCollection.getDB();
		final Set<String> collectionNames = database.getCollectionNames();
		// expect a collection for system.indexes, fs.files, fs.chunks, and
		// migrations because the GridFS is created early
		return collectionNames.size() <= 4;
	}
}
