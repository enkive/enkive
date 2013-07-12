package com.linuxbox.util.dbmigration;

import com.linuxbox.util.dbinfo.DbInfo;

public class NullDbMigration extends DbMigration {

	public NullDbMigration(DbMigrator migrator, int fromVersion, int toVersion) throws DbMigrationException {
		super(migrator, fromVersion, toVersion);
	}
	
	@Override
	public void migrate(DbInfo dbInfo)  {
		// since this is the null db migration, does nothing
		return;
	}
}
