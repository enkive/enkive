package com.linuxbox.enkive.tool.mongodb;

import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.linuxbox.enkive.Main;

public class MongoDbIndexManagerTool extends Main {
	/**
	 * The xml file to use if we need to build our own beans and context.
	 */
	static final String[] CONFIG_FILES = { "enkive-server.xml" };

	protected final static String DESCRIPTION = "com.linuxbox.enkive.tool.mongodb.MongoDBIndexManagerTool";

	public MongoDbIndexManagerTool(String[] arguments, String[] configFiles,
			String description) {
		super(arguments, configFiles, description, true, false);
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
