package com.linuxbox.util.dbmigration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class DBMigration {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.util.dbmigration.DBMigration.<SUBCLASS>");

	final int fromVersion;
	final int toVersion;
	final DBMigrator migrator;

	public DBMigration(DBMigrator migrator, int fromVersion, int toVersion)
			throws DBMigrationException {
		this.fromVersion = fromVersion;
		this.toVersion = toVersion;
		this.migrator = migrator;
		migrator.registerMigration(this);
	}

	public abstract boolean migrate(DBInfo db) throws DBMigrationException;
}