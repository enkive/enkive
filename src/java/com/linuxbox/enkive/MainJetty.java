package com.linuxbox.enkive;

public class MainJetty extends Main {
	static final String[] CONFIG_FILES = { "jetty-server.xml" };

	public MainJetty(String[] arguments) {
		super(CONFIG_FILES, arguments);
	}

	@Override
	protected void doEventLoop() {
		LOGGER.info("all systems up");
	}

	@Override
	protected void startup() {
		// empty for now
	}

	@Override
	protected void shutdown() {
		// empty for now
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main main = new MainJetty(args);
		main.run();
	}
}
