package com.linuxbox.util.dbmigration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;

import com.linuxbox.enkive.ProductInfo;
import com.linuxbox.enkive.exception.UnimplementedMethodException;
import com.linuxbox.util.Version;
import com.linuxbox.util.dbmigration.DbStatusRecord.Status;

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
	protected List<DbMigrator> migrators;
	protected boolean isRunning = false;
	protected boolean isMigrating = false;
	protected Runnable stopCallback;

	public DbMigrationService() {
		migrators = new ArrayList<DbMigrator>();
	}

	public void isUpToDate() throws UpToDateException {
		final int currentVersion = ProductInfo.VERSION.versionOrdinal;

		DbStatusRecord storedStatus = getLatestDbStatusRecord();
		if (storedStatus.status != Status.STORED) {
			throw new UpToDateException(
					"the database never completed its most recent migration to version ordinal "
							+ storedStatus.version + ", which began at "
							+ storedStatus.timestamp);
		}

		String storedVersionString = Version
				.versionStringFromOrdinal(storedStatus.version);

		if (storedStatus.version < currentVersion) {
			String message = "This version of Enkive ("
					+ ProductInfo.VERSION.versionString
					+ ") requires a migrated version of the Enkive database";
			if (storedVersionString != null) {
				message += ", which appears to be for version "
						+ storedVersionString;
			}
			message += ". Please run the DB Migration tool before starting Enkive.";

			throw new UpToDateException(message);
		} else if (storedStatus.version > currentVersion) {
			final String message = "This version of Enkive ("
					+ ProductInfo.VERSION.versionString
					+ ") appears to be too low given the current state of the Enkive database (ordinal "
					+ storedStatus.version
					+ "). Please make sure you are running the latest version of Enkive.";
			throw new UpToDateException(message);
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("database passed version check");
		}
	}

	public boolean loadAndCheckMigrators() {
		final int newVersion = ProductInfo.VERSION.versionOrdinal;
		String newVersionString = Version.versionStringFromOrdinal(newVersion);
		if (null == newVersionString) {
			newVersionString = "Ordinal(" + newVersion + ")";
		}

		final int oldVersion = getLatestDbStatusRecord().version;
		String oldVersionString = Version.versionStringFromOrdinal(oldVersion);
		if (null == oldVersionString) {
			oldVersionString = "Ordinal(" + oldVersion + ")";
		}

		boolean problem = false;

		Map<String, DbMigrator> loadedMigrators = applicationContext
				.getBeansOfType(DbMigrator.class);

		for (Entry<String, DbMigrator> e : loadedMigrators.entrySet()) {
			if (!e.getValue().canReachVersion(oldVersion, newVersion)) {
				LOGGER.error("Migrator \"" + e.getKey()
						+ "\" cannot reach version " + newVersionString
						+ " from version " + oldVersionString + ".");
				problem = true;
			}
		}

		migrators.addAll(loadedMigrators.values());

		return !problem;
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

			LOGGER.trace("would be running migrators here");

			// for (DBMigrator migrator : migrators) {
			// TODO run all the migrations
			// }'

		} finally {
			synchronized (this) {
				isMigrating = false;

				if (!isRunning && stopCallback != null) {
					stopCallback.run();
				}
			}
		}
	}

	abstract public DbStatusRecord getLatestDbStatusRecord();

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
