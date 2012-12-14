package com.linuxbox.enkive.statistics.migration;

import com.linuxbox.util.dbmigration.DBInfo;
import com.linuxbox.util.dbmigration.DBMigration;
import com.linuxbox.util.dbmigration.DBMigrationException;
import com.linuxbox.util.dbmigration.DBMigrator;
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
public class StatisticsMigration extends DBMigration {
	public StatisticsMigration(DBMigrator migrator, int fromVersion,
			int toVersion, Mongo m, String statisticsCollection)
			throws DBMigrationException {
		super(migrator, fromVersion, toVersion);
		this.enkive = m.getDB("enkive");
		this.statsColl = statisticsCollection;
	}
	
	DB enkive;
	String statsColl;
	
	@Override
	public boolean migrate(DBInfo db) throws DBMigrationException {
		enkive.getCollection(statsColl).drop();
		
		DBObject statsVersion = new BasicDBObject("service", "statistics");
		statsVersion.put("version", 2);
		
		enkive.getCollection("migratorService").insert(statsVersion);
		return true;
	}
}
