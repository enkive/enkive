package com.linuxbox.enkive;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.springframework.context.ApplicationContext;

import com.linuxbox.enkive.web.TestServlet;

public class MainJetty extends MainConsole {
	static final String[] CONFIG_FILES = { "jetty-server.xml" };

	public MainJetty(String[] arguments) {
		this(CONFIG_FILES, arguments);
	}

	public MainJetty(String[] configFiles, String[] arguments) {
		super(configFiles, arguments);
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

		ServletContextHandler servletContextHandler = context.getBean(
				"ServletContextHandler",
				org.eclipse.jetty.servlet.ServletContextHandler.class);

		servletContextHandler.addServlet(TestServlet.class, "/test");
		
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
