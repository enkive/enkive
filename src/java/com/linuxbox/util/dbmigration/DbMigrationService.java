package com.linuxbox.util.dbmigration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;

import com.linuxbox.enkive.ProductInfo;
import com.linuxbox.enkive.exception.UnimplementedMethodException;
import com.linuxbox.util.Version;
import com.linuxbox.util.dbmigration.DbStatusRecord.Status;
import com.linuxbox.util.dbmigration.DbVersionManager.DbVersion;
import com.linuxbox.util.dbmigration.DbVersionManager.DbVersionManagerException;

/**
 * 
 */
public abstract class DbMigrationService implements ApplicationContextAware,
		SmartLifecycle {
	public static class UpToDateException extends Exception {
		private static final long serialVersionUID = 7934565156056935617L;

		public UpToDateException(String message) {
			super(message);
		}
	}

	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.util.dbmigration.DBMigrationService");

	private ApplicationContext applicationContext;
	protected DbVersionManager dbVersionManager;
	protected List<DbMigrator> migrators;
	protected boolean isRunning = false;
	protected boolean isMigrating = false;
	protected Runnable stopCallback;

	public DbMigrationService() {
		migrators = new ArrayList<DbMigrator>();
	}

	public boolean isUpToDateTest() {
		try {
			isUpToDate();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void isUpToDate() throws UpToDateException,
			DbVersionManagerException {
		final Version softwareVersion = ProductInfo.VERSION;
		final DbVersion requiredDbVersion = dbVersionManager
				.appropriateDbVersionFor(softwareVersion);

		DbStatusRecord storedStatus = getLatestDbStatusRecord();
		if (storedStatus.status != Status.STORED) {
			throw new UpToDateException(
					"The database never completed its most recent migration to version ordinal "
							+ storedStatus.dbVersion
							+ ", which began at "
							+ storedStatus.timestamp
							+ ". It is recommended that you restore your database from backup and then rerun the migration.");
		}

		String storedVersionString = dbVersionManager
				.softwareVersionsAppropriateToDbVersion(storedStatus.dbVersion);

		if (storedStatus.dbVersion.precedes(requiredDbVersion)) {
			String message = "This version of Enkive ("
					+ ProductInfo.VERSION.versionString
					+ ") requires a migrated version of the Enkive database";
			if (storedVersionString != null) {
				message += ", which appears to be for version(s) "
						+ storedVersionString;
			}
			message += ". Please run the DB Migration tool before starting Enkive.";

			throw new UpToDateException(message);
		} else if (requiredDbVersion.precedes(storedStatus.dbVersion)) {
			final String message = "This version of Enkive ("
					+ ProductInfo.VERSION.versionString
					+ ") appears to be too low given the current state of the Enkive database (ordinal "
					+ storedStatus.dbVersion
					+ "). Please make sure you are running the latest version of Enkive.";
			throw new UpToDateException(message);
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("database passed version check");
		}
	}

	/**
	 * Load all the migrators and verify that we can go from the current state
	 * to the desired state.
	 * 
	 * @return true if no problems, false if there is a problem
	 */
	public boolean loadAndCheckMigrators() {
		try {
			final Version thisSoftwareVersion = ProductInfo.VERSION;
			final DbVersion thisDbVersion = dbVersionManager
					.appropriateDbVersionFor(thisSoftwareVersion);
			final String thisDbVersionString = dbVersionManager
					.softwareVersionsAppropriateToDbVersion(thisDbVersion,
							"UNKNOWN");

			final DbStatusRecord dbStatus = getLatestDbStatusRecord();
			final String dbStatusVersionString = dbVersionManager
					.softwareVersionsAppropriateToDbVersion(dbStatus.dbVersion,
							"UNKNOWN");

			boolean problem = false;

			Map<String, DbMigrator> loadedMigrators = applicationContext
					.getBeansOfType(DbMigrator.class);

			for (Entry<String, DbMigrator> e : loadedMigrators.entrySet()) {
				final DbMigrator migrator = e.getValue();
				if (!migrator.canReachVersion(dbStatus.dbVersion.ordinal,
						thisDbVersion.ordinal)) {
					LOGGER.error("Migrator \"" + e.getKey()
							+ "\" cannot reach version "
							+ thisDbVersion.ordinal + " (software version: "
							+ thisDbVersionString + ") from version "
							+ dbStatus.dbVersion.ordinal
							+ " (software version: " + dbStatusVersionString
							+ ").");
					problem = true;
				}
			}

			migrators.addAll(loadedMigrators.values());

			return !problem;
		} catch (DbVersionManagerException e) {
			LOGGER.error(e);
			return false;
		}
	}

	public void migrate() throws DbMigrationException {
		synchronized (this) {
			if (isMigrating) {
				throw new DbMigrationException(
						"cannot start new migration when one is already underway");
			}

			if (!isRunning) {
				throw new DbMigrationException(
						"cannot start migrating once DBMigrationService has been requested to stop running");
			}

			isMigrating = true;
		}

		try {
			if (!loadAndCheckMigrators()) {
				throw new DbMigrationException(
						"One or more of the migrators will not work; see log.");
			}

			final Version thisSoftwareVersion = ProductInfo.VERSION;
			final DbVersion neededDbVersion = dbVersionManager
					.appropriateDbVersionFor(thisSoftwareVersion);

			final DbStatusRecord dbStatus = getLatestDbStatusRecord();

			try {
				// record that we're starting migration
				addDbStatusRecord(new DbStatusRecord(neededDbVersion,
						Status.MIGRATING, new Date()));

				for (DbMigrator migrator : migrators) {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Starting migrations for "
								+ migrator.migratorName + "...");
					}

					final int resultingVersion = migrator
							.runThrough(dbStatus.dbVersion.ordinal,
									neededDbVersion.ordinal);
					if (resultingVersion != neededDbVersion.ordinal) {
						final String errorMessage = "For migrator "
								+ migrator.migratorName
								+ " could only migrate from version "
								+ dbStatus.dbVersion.ordinal
								+ " to version "
								+ resultingVersion
								+ ", which did not achieve the desired version of "
								+ neededDbVersion.ordinal + ".";

						LOGGER.fatal(errorMessage);
						throw new DbMigrationException(errorMessage);
					}

					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Finished migrations for "
								+ migrator.migratorName + ".");
					}
				}

				// record that we finished migration successfully
				addDbStatusRecord(new DbStatusRecord(neededDbVersion,
						Status.STORED, new Date()));

				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Finished all migrations to bring database to version "
							+ neededDbVersion.ordinal);
				}
			} catch (DbMigrationException e) {
				// record that we got an error in completing migration
				addDbStatusRecord(new DbStatusRecord(neededDbVersion,
						Status.ERROR, new Date()));
				throw e;
			}
		} catch (DbVersionManagerException e) {
			throw new DbMigrationException("Error with DbVersionManager", e);
		} finally {
			synchronized (this) {
				isMigrating = false;

				if (!isRunning && stopCallback != null) {
					stopCallback.run();
				}
			}
		}
	}

	@Required
	public void setDbVersionManager(DbVersionManager manager) {
		this.dbVersionManager = manager;
	}

	abstract public DbStatusRecord getLatestDbStatusRecord()
			throws DbVersionManagerException;

	abstract public void addDbStatusRecord(DbStatusRecord record);

	/*
	 * Methods for ApplicationContextAware
	 */

	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.applicationContext = context;
	}

	/*
	 * Methods for SmartLifecyle
	 */

	@Override
	public void start() {
		isRunning = true;
	}

	@Override
	public void stop() {
		throw new UnimplementedMethodException();
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		synchronized (this) {
			isRunning = false;
			if (isMigrating) {
				stopCallback = callback;
			} else {
				callback.run();
			}
		}
	}
}
