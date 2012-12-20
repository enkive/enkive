package com.linuxbox.util.dbmigration;

public abstract class DBMigration {
	int fromVersion;
	int toVersion;
	DBMigrator migrator;

	public DBMigration(DBMigrator migrator, int fromVersion, int toVersion)
			throws DBMigrationException {
		this.fromVersion = fromVersion;
		this.toVersion = toVersion;
		this.migrator = migrator;
		migrator.registerMigration(this);
	}

	public abstract boolean migrate(DBInfo db) throws DBMigrationException;
}