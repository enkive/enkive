package com.linuxbox.enkive.statistics.migration;

import com.linuxbox.util.dbinfo.DbInfo;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.linuxbox.util.dbmigration.DbMigration;
import com.linuxbox.util.dbmigration.DbMigrationException;
import com.linuxbox.util.dbmigration.DbMigrator;

/**
 * Migrates the stats data from 1.2 to 1.3 (deletes the bad statistics data)
 */
public class StatsMigration0to1 extends DbMigration {
	public StatsMigration0to1(DbMigrator migrator) throws DbMigrationException {
		super(migrator, 0, 1);
	}

	@Override
	public boolean migrate(DbInfo dbInfo) throws DbMigrationException {
		MongoDbInfo mongoDbInfo = (MongoDbInfo) dbInfo;
		LOGGER.info("Running statistics migration 0 to 1");
		// we don't care about the data so we don't need to stash it while
		// emptying the DB; we also don't need to recreate the collection as the
		// statistics service will do that automatically for us
		mongoDbInfo.getCollection().drop();
		return true;
	}
}
