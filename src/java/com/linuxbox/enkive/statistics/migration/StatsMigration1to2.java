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
public class StatsMigration1to2 extends DBMigration {
	DB enkive;
	String statsColl;
	
	public StatsMigration1to2(DBMigrator migrator, Mongo m, String statisticsCollection)
			throws DBMigrationException {
		super(migrator, 1, 2);
		this.enkive = m.getDB("enkive");
		this.statsColl = statisticsCollection;
	}
	
	@Override
	public boolean migrate(DBInfo db) throws DBMigrationException {
		if(db.getCurrentVersion() >= 2){
			return false;
		}
		
		//we don't care about the data so we don't need to stash it while emptying the DB
		//we also don't need to recreate the collection as the statistics service will
		//do that automatically for us
		enkive.getCollection(statsColl).drop();
		
		DBObject statsVersion = new BasicDBObject("service", "statistics");
		statsVersion.put("version", 2);
		
		enkive.getCollection("migratorService").insert(statsVersion);
		return true;
	}
}
