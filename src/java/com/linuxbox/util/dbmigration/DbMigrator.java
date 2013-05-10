package com.linuxbox.util.dbmigration;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.util.dbinfo.DbInfo;

public class DbMigrator {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.util.dbmigration.DBMigrator");

	protected Map<Integer, DbMigration> migrations = new HashMap<Integer, DbMigration>();
	protected String migratorName;
	protected DbInfo dbInfo;

	protected DbMigrator(String migratorName, DbInfo dbInfo) {
		this.migratorName = migratorName;
		this.dbInfo = dbInfo;
	}

	public void registerMigration(DbMigration migration)
			throws DbMigrationException {
		if (migrations.containsValue(migration.fromVersion)) {
			throw new DbMigrationException("already have migration for "
					+ migratorName + " from version " + migration.fromVersion);
		}
		migrations.put(migration.fromVersion, migration);
	}

	public int runThrough(final int fromVersion, final int toVersion)
			throws DbMigrationException {
		int currentVersion = fromVersion;
		while (currentVersion < toVersion) {
			Integer newVersion = runNext(currentVersion);
			if (newVersion == null) {
				throw new DbMigrationException(migratorName
						+ " could not find the migration for version "
						+ currentVersion);
			}
			currentVersion = newVersion;
		}
		return currentVersion;
	}

	public int runAll(int fromVersion) throws DbMigrationException {
		Integer lastVersion = fromVersion;
		Integer newVersion = runNext(lastVersion);
		while (newVersion != null) {
			lastVersion = newVersion;
			newVersion = runNext(lastVersion);
		}
		return lastVersion;
	}

	Integer runNext(int fromVersion) throws DbMigrationException {
		DbMigration nextMigration = migrations.get(fromVersion);
		if (nextMigration == null) {
			return null;
		} else {
			nextMigration.migrate(dbInfo);
			return nextMigration.toVersion;
		}
	}

	boolean canReachVersion(int startingVersion, int goalVersion) {
		if (startingVersion == goalVersion) {
			return true;
		}

		DbMigration prevMigration = null;
		DbMigration nextMigration = migrations.get(startingVersion);
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
		DbMigration prevMigration = null;
		DbMigration nextMigration = migrations.get(startingVersion);
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
