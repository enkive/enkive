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

/**
 * Due to the low phase, this service should start up first.
 * 
 * @author eric
 * 
 */
public class DBMigrationService implements ApplicationContextAware,
		SmartLifecycle {
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

	// FIXME was @PostConstruct
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

	/*
	 * Methods needed to implement the SmartLifecycle interface.
	 */

	@Override
	public int getPhase() {
		LOGGER.fatal("getPhase called");
		return -10;
	}

	@Override
	public void start() {
		LOGGER.info("DBMigration service is starting.");
		isRunning = true;
	}

	@Override
	public void stop() {
		// do other stuff up here
		
		isRunning = false;
		LOGGER.info("DBMigration service has stopped.");
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}
}
