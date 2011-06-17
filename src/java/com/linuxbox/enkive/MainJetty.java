package com.linuxbox.enkive;

public class MainJetty extends MainConsole {
	static final String[] CONFIG_FILES = { "jetty-server.xml" };

	public MainJetty(String[] arguments) {
		super(CONFIG_FILES, arguments);
	}

	@Override
	protected void startup() {
		super.startup();
		out.println("Jetty started");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main main = new MainJetty(args);
		main.run();
	}
}
