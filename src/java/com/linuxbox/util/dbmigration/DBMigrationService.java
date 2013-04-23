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

import com.linuxbox.enkive.ProductInfo;
import com.linuxbox.util.Version;
import com.linuxbox.util.dbmigration.DBStatusRecord.Status;

/**
 * 
 */
public abstract class DBMigrationService implements ApplicationContextAware {
	public static class UpToDateException extends Exception {
		private static final long serialVersionUID = 7934565156056935617L;

		public UpToDateException(String message) {
			super(message);
		}
	}

	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.util.dbmigration.DBMigrationService");

	private ApplicationContext applicationContext;
	protected List<DBMigrator> migrators;
	protected boolean isRunning = false;

	public DBMigrationService() {
		migrators = new ArrayList<DBMigrator>();
	}

	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.applicationContext = context;
		runMigrators();
	}

	public void isUpToDate() throws UpToDateException {
		final int currentVersion = ProductInfo.VERSION.versionOrdinal;

		DBStatusRecord storedStatus = getLatestDbStatusRecord();
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
					+ ") requires a migrated version of the Enkive database ";
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

	public void runMigrators() {
		Map<String, DBMigrator> migrators = applicationContext
				.getBeansOfType(DBMigrator.class);

		LOGGER.info("Load up migrators.");
		for (Entry<String, DBMigrator> e : migrators.entrySet()) {
			LOGGER.info("Aware of migrator " + e.getKey());
		}
		this.migrators.addAll(migrators.values());

		// for (DBMigrator migrator : migrators) {
		// TODO run all the migrations
		// }
	}

	abstract public DBStatusRecord getLatestDbStatusRecord();
}
