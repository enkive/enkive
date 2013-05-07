package com.linuxbox.util.dbmigration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.util.dbinfo.DbInfo;

public abstract class DbMigration {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.util.dbmigration.DBMigration.<SUBCLASS>");

	final int fromVersion;
	final int toVersion;
	final DbMigrator migrator;

	public DbMigration(DbMigrator migrator, int fromVersion, int toVersion)
			throws DbMigrationException {
		this.fromVersion = fromVersion;
		this.toVersion = toVersion;
		this.migrator = migrator;
		migrator.registerMigration(this);
	}

	public abstract boolean migrate(DbInfo db) throws DbMigrationException;
}