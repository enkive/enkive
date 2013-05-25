/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.linuxbox.enkive;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.audit.NullAuditService;
import com.linuxbox.enkive.tool.mongodb.MongoDbIndexManager;
import com.linuxbox.util.DirectoryManagement;
import com.linuxbox.util.dbmigration.DbMigrationService;

public abstract class Main {
	protected static final Log LOGGER;
	private static final String USER = AuditService.USER_SYSTEM;
	private static final String[] VERSION_CHECK_CONFIG_FILES = { "enkive-version-check.xml" };
	protected static final String[] NO_ARGS = {};

	protected AbstractApplicationContext context;
	protected AuditService auditService = null;

	final protected String[] arguments;
	final protected String[] configFiles;
	final protected boolean runVersionCheck;
	final protected boolean runIndexCheck;
	final protected boolean runAuditService;
	final protected String description;

	protected abstract void runCoreFunctionality(ApplicationContext context)
			throws Exception;

	protected abstract void preStartup();

	protected abstract void postStartup();

	protected abstract void preShutdown();

	protected abstract void postShutdown();

	static {
		try {
			DirectoryManagement.verifyDirectory(
					GeneralConstants.DEFAULT_LOG_DIRECTORY,
					"default logging directory");
		} catch (IOException e) {
			throw new Error(e);
		}
		LOGGER = LogFactory.getLog("com.linuxbox.enkive");
	}

	public Main(String[] arguments, String[] configFiles, String description) {
		this(arguments, configFiles, description, true, true, true);
	}

	public Main(String[] arguments, String[] configFiles, String description,
			boolean runVersionCheck, boolean runIndexCheck,
			boolean runAuditService) {
		this.arguments = arguments;
		this.configFiles = configFiles;
		this.description = description;
		this.runVersionCheck = runVersionCheck;
		this.runIndexCheck = runIndexCheck;
		this.runAuditService = runAuditService;
	}

	protected void runVersionCheck() throws Exception {
		final AbstractApplicationContext versionCheckingContext = new ClassPathXmlApplicationContext(
				VERSION_CHECK_CONFIG_FILES);
		try {
			Map<String, DbMigrationService> migrationServices = versionCheckingContext
					.getBeansOfType(DbMigrationService.class);
			if (migrationServices.isEmpty()) {
				final String message = "no version checking / migration services configured";
				LOGGER.fatal(message);
				throw new Exception(message);
			}
			for (Entry<String, DbMigrationService> service : migrationServices
					.entrySet()) {
				service.getValue().isUpToDate();
			}
		} finally {
			versionCheckingContext.close();
		}
	}

	public void start() throws Exception {
		preStartup();

		if (runVersionCheck) {
			runVersionCheck();
		}

		/*
		 * IF WE GET HERE AND runVersionCheck IS TRUE, THE DATABASE IS
		 * APPARENTLY UP TO DATE
		 */

		context = new ClassPathXmlApplicationContext(configFiles);
		context.registerShutdownHook();

		if (runAuditService) {
			auditService = context.getBean("AuditLogService",
					AuditService.class);
		} else {
			// since no audit service is wanted, simply instantiate the null
			// version and let it throw away the log entries
			auditService = new NullAuditService();
		}

		auditService.addEvent(AuditService.SYSTEM_STARTUP, USER, description);

		if (runIndexCheck) {
			final MongoDbIndexManager mongoIndexManager = context
					.getBean(MongoDbIndexManager.class);
			mongoIndexManager.runCheckAndAutoEnsure();
		}

		postStartup();
	}

	public void stop() throws Exception {
		try {
			auditService.addEvent(AuditService.SYSTEM_SHUTDOWN, USER,
					description);
		} catch (AuditServiceException e) {
			LOGGER.error("received AuditServiceException: " + e.getMessage(), e);
		}

		preShutdown();

		context.close();

		postShutdown();
	}

	public void run() throws Exception {
		start();
		try {
			runCoreFunctionality(context);
		} finally {
			stop();
		}
	}
}
