package com.linuxbox.util.dbmigration;

public interface DBInfo {
	int getCurrentVersion() throws DBMigrationException;
}
