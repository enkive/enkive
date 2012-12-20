package com.linuxbox.enkive.statistics.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.util.dbmigration.DBInfo;
import com.linuxbox.util.dbmigration.DBMigration;
import com.linuxbox.util.dbmigration.DBMigrationException;
import com.linuxbox.util.dbmigration.DBMigrator;
import com.linuxbox.util.dbmigration.mongodb.MongoDBInfo;


/**
 * Migrates the stats data from 1.2 to 1.2.1 (deletes the bad statistics data)
 * 
 * @author noah
 * 
 */
public class StatsMigration1to2 extends DBMigration {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.migration.StatsMigration1to2");

	public StatsMigration1to2(DBMigrator migrator) throws DBMigrationException {
		super(migrator, 1, 2);
	}

	@Override
	public boolean migrate(DBInfo db) throws DBMigrationException {
		MongoDBInfo dbInfo = (MongoDBInfo) db;

		// NOAH: I'm wondering about this check -- theoretically this will only
		// get called by the migrator who should be checking the versions; but
		// the method is public, and I'm not sure we can tighten that
		if (db.getCurrentVersion() >= 2) {
			return false;
		}

		LOGGER.info("Running statistics migration 1 to 2");
		// we don't care about the data so we don't need to stash it while
		// emptying the DB
		// we also don't need to recreate the collection as the statistics
		// service will
		// do that automatically for us
		dbInfo.getCollection().drop();

		// NOAH -- this is old; use the DBInfo instead
		// enkive.getCollection(statsColl).drop();

		// NOAH: the following should be better automated; since the migrator is
		// calling this and it should know what version it's taking it to and it
		// should know about the migrator service in general, *it* should update
		// the version number stored in the back end. Otherwise we'd have to
		// repeat this type of code for every single migration!!!

		// DBObject statsVersion = new BasicDBObject("service", "statistics");
		// statsVersion.put("version", 2);

		// enkive.getCollection("migratorService").insert(statsVersion);
		return true;
	}
}
