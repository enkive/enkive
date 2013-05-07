package com.linuxbox.enkive.tool.mongodb;

import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.linuxbox.enkive.Main;

public class MongoDbIndexManagerTool extends Main {
	/**
	 * The xml file to use if we need to build our own beans and context.
	 */
	protected final static String[] CONFIG_FILES = { "enkive-server.xml" };
	
	protected final static boolean RUN_VERSION_CHECK = true;
	protected final static boolean RUN_INDEX_CHECK = false;
	protected final static boolean RUN_AUDIT_SERVICE = true;

	protected final static String DESCRIPTION = "com.linuxbox.enkive.tool.mongodb.MongoDBIndexManagerTool";

	public MongoDbIndexManagerTool(String[] arguments, String[] configFiles,
			String description) {
		super(arguments, configFiles, description, RUN_VERSION_CHECK,
				RUN_INDEX_CHECK, RUN_AUDIT_SERVICE);
	}

	public static void main(String[] args) {
		try {
			MongoDbIndexManagerTool manager = new MongoDbIndexManagerTool(args,
					CONFIG_FILES, DESCRIPTION);
			manager.run();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	protected void runCoreFunctionality(ApplicationContext context) {
		Map<String, MongoDbIndexManager> map = context
				.getBeansOfType(MongoDbIndexManager.class);
		if (map.size() == 0) {
			System.err
					.println("Error: did not find bean of type MongoDBIndexManager.");
			return;
		} else if (map.size() > 1) {
			System.err
					.println("Error: found multiple beans of type MongoDBIndexManager.");
			return;
		}

		// even though this is a loop, it should only run one time due to checks
		// above
		map.values().iterator().next().runConsole();
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
}
