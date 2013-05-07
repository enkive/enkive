package com.linuxbox.enkive.tool;

import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.linuxbox.enkive.Main;
import com.linuxbox.util.dbmigration.DbMigrationException;
import com.linuxbox.util.dbmigration.DbMigrationService;

public class DbMigrationTool extends Main {
	protected final static String DESCRIPTION = "com.linuxbox.enkive.tool.DBMigrationTool";
	protected final static String[] CONFIG_FILES = {};

	public DbMigrationTool(String[] arguments) {
		super(arguments, CONFIG_FILES, DESCRIPTION, false, false);
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

		migrationService.migrate();
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
		} catch (Exception e) {
			LOGGER.error("Error running " + DESCRIPTION, e);
			System.err.println("Error: " + e.getMessage());
		}
	}
}
