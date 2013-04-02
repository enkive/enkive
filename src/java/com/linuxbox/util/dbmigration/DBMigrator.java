package com.linuxbox.util.dbmigration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.Lifecycle;

public class DBMigrator implements Lifecycle {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.util.dbmigration.DBMigrator");

	protected Map<Integer, DBMigration> migrations = new HashMap<Integer, DBMigration>();
	protected String migratorName;
	protected DBInfo db;
	protected boolean isRunning;

	protected DBMigrator(String migratorName, DBInfo db, String defaultPackage,
			List<String> migrations) {
		this.migratorName = migratorName;
		this.db = db;

		for (String s : migrations) {
			LOGGER.info("migrator: " + s);
		}
	}

	public void registerMigration(DBMigration migration)
			throws DBMigrationException {
		if (migrations.containsValue(migration.fromVersion)) {
			throw new DBMigrationException("already have migration for "
					+ migratorName + " from version " + migration.fromVersion);
		}
		migrations.put(migration.fromVersion, migration);
	}

	public int runThrough(int fromVersion, int toVersion)
			throws DBMigrationException {
		while (fromVersion < toVersion) {
			Integer newVersion = runNext(fromVersion);
			if (newVersion == null) {
				throw new DBMigrationException(migratorName
						+ " could not find the migration for version "
						+ fromVersion);
			}
			fromVersion = newVersion;
		}
		return toVersion;
	}

	// TODO this will be replaced by the DBMigrationService
	@PostConstruct
	public void init() {
		LOGGER.info("Running " + migratorName);
		try {
			runAll(db.getCurrentVersion());
		} catch (DBMigrationException e) {
			e.printStackTrace();
		}
	}

	public int runAll(int fromVersion) throws DBMigrationException {
		Integer lastVersion = fromVersion;
		Integer newVersion = runNext(lastVersion);
		while (newVersion != null) {
			lastVersion = newVersion;
			newVersion = runNext(lastVersion);
		}
		return lastVersion;
	}

	Integer runNext(int fromVersion) throws DBMigrationException {
		DBMigration nextMigration = migrations.get(fromVersion);
		if (nextMigration == null) {
			return null;
		} else {
			nextMigration.migrate(db);
			// TODO Update version here?
			return nextMigration.toVersion;
		}
	}

	@Override
	public void start() {
		isRunning = true;
		LOGGER.info("DBMigrator " + migratorName + " started.");
	}

	@Override
	public void stop() {
		isRunning = false;
		LOGGER.info("DBMigrator " + migratorName + " stopped.");
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}
}
