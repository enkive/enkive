package com.linuxbox.enkive.statistics.services.storage.mongodb;

import com.linuxbox.util.dbmigration.DBInfo;
import com.linuxbox.util.dbmigration.DBMigrator;

public class MongoStatsMigrator extends DBMigrator {
	static final String MIGRATOR_NAME = "Stats Mongo Migrator";

	public MongoStatsMigrator(DBInfo db) {
		super(MIGRATOR_NAME, db);
	}

}
