package com.linuxbox.util.dbmigration;

import java.util.HashMap;
import java.util.Map;

public abstract class DBMigration {
	static Map<Integer, DBMigration> migrations = new HashMap<Integer, DBMigration>();
	int fromVersion;
	int toVersion;

	public DBMigration(DBMigrator migrator, int fromVersion, int toVersion)
			throws DBMigrationException {
		this.fromVersion = fromVersion;
		this.toVersion = toVersion;
		migrator.registerMigration(this);
	}

	public abstract boolean migrate(DBInfo db) throws DBMigrationException;
}