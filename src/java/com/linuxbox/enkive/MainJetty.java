package com.linuxbox.enkive;

import org.eclipse.jetty.deploy.providers.ContextProvider;
import org.springframework.context.ApplicationContext;

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

	protected void doEventLoop(ApplicationContext context) {
		org.mortbay.jetty.spring.Server server = context.getBean("Server",
				org.mortbay.jetty.spring.Server.class);
		ContextProvider provider = server.getBean(ContextProvider.class);

		/*
		 * ContextProvider provider = context.getBean("ContextProvider",
		 * ContextProvider.class);
		 */

		// provider
		super.doEventLoop(context);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main main = new MainJetty(args);
		main.run();
	}
}
