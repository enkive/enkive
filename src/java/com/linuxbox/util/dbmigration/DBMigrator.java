package com.linuxbox.util.dbmigration;

import java.util.HashMap;
import java.util.Map;

public class DBMigrator {
	Map<Integer, DBMigration> migrations = new HashMap<Integer, DBMigration>();
	String migratorName;
	DBInfo db;

	public DBMigrator(String migratorName, DBInfo db) {
		this.migratorName = migratorName;
		this.db = db;
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
				//TODO put stuff V here V
				throw new DBMigrationException(
						"Noah, please put something here");
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
			nextMigration.migrate(db);
			return nextMigration.toVersion;
		}
	}
}
