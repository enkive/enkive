package com.linuxbox.util.dbmigration;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.util.dbinfo.DbInfo;

public class DBMigrator {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.util.dbmigration.DBMigrator");

	protected Map<Integer, DBMigration> migrations = new HashMap<Integer, DBMigration>();
	protected String migratorName;
	protected DbInfo dbInfo;

	protected DBMigrator(String migratorName, DbInfo dbInfo) {
		this.migratorName = migratorName;
		this.dbInfo = dbInfo;
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
			nextMigration.migrate(dbInfo);
			// TODO Update version here?
			return nextMigration.toVersion;
		}
	}

	boolean canReachVersion(int startingVersion, int goalVersion) {
		if (startingVersion == goalVersion) {
			return true;
		}

		DBMigration prevMigration = null;
		DBMigration nextMigration = migrations.get(startingVersion);
		while (nextMigration != null) {
			if (nextMigration.toVersion == goalVersion) {
				return true;
			}
			prevMigration = nextMigration;
			nextMigration = migrations.get(prevMigration.toVersion);
		}

		return false;
	}

	int lastReachableVersionFrom(int startingVersion) {
		DBMigration prevMigration = null;
		DBMigration nextMigration = migrations.get(startingVersion);
		while (nextMigration != null) {
			prevMigration = nextMigration;
			nextMigration = migrations.get(prevMigration.toVersion);
		}

		if (prevMigration == null) {
			return startingVersion;
		} else {
			return prevMigration.toVersion;
		}
	}
}
