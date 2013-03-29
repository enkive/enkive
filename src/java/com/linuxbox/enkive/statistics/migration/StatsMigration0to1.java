package com.linuxbox.enkive.statistics.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.Order;

import com.linuxbox.util.dbmigration.DBInfo;
import com.linuxbox.util.dbmigration.DBMigration;
import com.linuxbox.util.dbmigration.DBMigrationException;
import com.linuxbox.util.dbmigration.DBMigrator;
import com.linuxbox.util.dbmigration.mongodb.MongoDBInfo;

/**
 * Migrates the stats data from 1.2 to 1.3 (deletes the bad statistics data)
 * 
 * @author noah
 * 
 */
@Order(1)
public class StatsMigration0to1 extends DBMigration {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.migration.StatsMigration0to1");

	public StatsMigration0to1(DBMigrator migrator) throws DBMigrationException {
		super(migrator, 0, 1);
	}

	@Override
	public boolean migrate(DBInfo dbInfo) throws DBMigrationException {
		MongoDBInfo mongoDbInfo = (MongoDBInfo) dbInfo;
		LOGGER.info("Running statistics migration 1 to 2");
		// we don't care about the data so we don't need to stash it while
		// emptying the DB
		// we also don't need to recreate the collection as the statistics
		// service will do that automatically for us
		mongoDbInfo.getCollection().drop();
		return true;
	}
}
