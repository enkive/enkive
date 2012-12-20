package com.linuxbox.enkive.statistics.migration;

import static com.linuxbox.enkive.statistics.migration.StatsMigrationConstants.STATS_SERVICE_NAME;
import static com.linuxbox.util.dbmigration.DBMigrationConstants.SERVICE;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.util.dbmigration.DBInfo;
import com.linuxbox.util.dbmigration.DBMigration;
import com.linuxbox.util.dbmigration.DBMigrationException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 * Migrates the stats data from 1.2 to 1.2.1 (deletes the bad statistics data)
 * 
 * @author noah
 * 
 */
public class StatsMigration1to2 extends DBMigration {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.migration.StatsMigration1to2");
	DB enkive;
	String statsColl;

	public StatsMigration1to2(Mongo m, String dbName,
			String statisticsCollection) throws DBMigrationException {
		// super(migrator, 1, 2);
		super(1, 2);
		this.enkive = m.getDB(dbName);
		this.statsColl = statisticsCollection;
	}

	@Override
	public boolean migrate(DBInfo db) throws DBMigrationException {
		if (db.getCurrentVersion() >= 2) {
			return false;
		}

		LOGGER.info("Running statistics migration 1 to 2");
		// we don't care about the data so we don't need to stash it while
		// emptying the DB
		// we also don't need to recreate the collection as the statistics
		// service will
		// do that automatically for us
		enkive.getCollection(statsColl).drop();

		DBObject statsVersion = new BasicDBObject(SERVICE, STATS_SERVICE_NAME);
		statsVersion.put("version", 2);

		enkive.getCollection("migratorService").insert(statsVersion);
		return true;
	}
}
