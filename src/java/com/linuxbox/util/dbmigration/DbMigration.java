package com.linuxbox.util.dbmigration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.util.dbinfo.DbInfo;

public abstract class DbMigration {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.util.dbmigration.DBMigration.<SUBCLASS>");

	final int fromVersion;
	final int toVersion;
	final DbMigrator migrator;
	protected final Properties props;

	public DbMigration(DbMigrator migrator, int fromVersion, int toVersion)
			throws DbMigrationException {
		this.fromVersion = fromVersion;
		this.toVersion = toVersion;
		this.migrator = migrator;
		try {
			this.props = loadConfigProperties();
		} catch (IOException e) {
			throw new DbMigrationException("Failed to create migration: ", e);
		}
		migrator.registerMigration(this);
	}

	/**
	 * Does the migration on the provided database.
	 * @param dbInfo
	 * @throws DbMigrationException
	 */
	public abstract void migrate(DbInfo dbInfo) throws DbMigrationException;

	static Properties loadConfigProperties() throws IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream("config/default/enkive.properties"));
		properties.load(new FileInputStream("config/enkive.properties"));
		return properties;
	}
}
