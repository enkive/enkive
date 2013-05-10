package com.linuxbox.enkive.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.linuxbox.enkive.Main;
import com.linuxbox.util.dbmigration.DbMigrationException;
import com.linuxbox.util.dbmigration.DbMigrationService;

public class DbMigrationTool extends Main {
	protected final static String DESCRIPTION = "com.linuxbox.enkive.tool.DBMigrationTool";
	protected final static String[] CONFIG_FILES = { "enkive-migration.xml" };
	protected final static boolean RUN_VERSION_CHECK = false;
	protected final static boolean RUN_INDEX_CHECK = false;
	protected final static boolean RUN_AUDIT_SERVICE = false;

	public DbMigrationTool(String[] arguments) {
		super(arguments, CONFIG_FILES, DESCRIPTION, RUN_VERSION_CHECK,
				RUN_INDEX_CHECK, RUN_AUDIT_SERVICE);
	}

	@Override
	protected void runCoreFunctionality(ApplicationContext context)
			throws DbMigrationException {
		final Map<String, DbMigrationService> migrationServices = context
				.getBeansOfType(DbMigrationService.class);
		if (migrationServices.size() == 0) {
			throw new DbMigrationException("no migration services found");
		} else if (migrationServices.size() > 1) {
			throw new DbMigrationException("multiple "
					+ migrationServices.size() + " migration services found");
		}

		final DbMigrationService migrationService = migrationServices.values()
				.iterator().next();

		if (migrationService.isUpToDateTest()) {
			System.out
					.println("The database appears to be up to date. No migrations run.");
		} else {
			System.out
					.println("The database appears to need migrations. It is important to create a backup of the database before running migrations.");

			System.out
					.print("Do you confirm that you have created a full backup of the database (YES/no)? ");

			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						System.in));
				String response = in.readLine().trim();
				if (!"YES".equals(response)) {
					throw new DbMigrationException(
							"User did not confirm that a backup of the database has been made prior to running migrations. S/he entered \""
									+ response + "\" rather than \"YES\".");
				}
			} catch (IOException e) {
				throw new DbMigrationException(
						"Was unable to read a confirmation from the user.", e);
			}

			// We should only be able to get here if the user responded "YES".

			migrationService.migrate();

			System.out
					.println("The migrations have now completed. Checking database status again....");
			if (!migrationService.isUpToDateTest()) {
				throw new DbMigrationException(
						"Database is not up to date after migrations run sucessfully.");
			} else {
				System.out
						.println("The database now appears to be up to date.");
			}
		}
	}

	@Override
	protected void preStartup() {
		// empty
	}

	@Override
	protected void postStartup() {
		// empty
	}

	@Override
	protected void preShutdown() {
		// empty
	}

	@Override
	protected void postShutdown() {
		// empty
	}

	public static void main(String[] arguments) {
		try {
			final DbMigrationTool tool = new DbMigrationTool(arguments);
			tool.run();
		} catch (DbMigrationException e) {
			System.err.println("Error: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			LOGGER.fatal("Error running " + DESCRIPTION, e);
		}
	}
}
