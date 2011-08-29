package com.linuxbox.enkive;

import org.eclipse.jetty.server.Server;
import org.springframework.context.ApplicationContext;

public class MainJettyWebApps extends MainConsole {
	static final String[] CONFIG_FILES = { "jetty-server-webapps.xml" };

	public MainJettyWebApps(String[] arguments) {
		super(CONFIG_FILES, arguments);
	}

	@Override
	protected void startup() {
		super.startup();
		out.println("Jetty will be starting...");
	}

	protected void doEventLoop(ApplicationContext context) {
		@SuppressWarnings("unused")
		Server server = context.getBean("Server",
				org.eclipse.jetty.server.Server.class);

		super.doEventLoop(context);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO attempt to change classpath
		// System.setProperty("jetty.class.path", "./bin/java");
		Main main = new MainJettyWebApps(args);
		main.run();
	}
}
