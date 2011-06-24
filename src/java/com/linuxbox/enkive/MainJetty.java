package com.linuxbox.enkive;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.springframework.context.ApplicationContext;

public class MainJetty extends MainConsole {
	public static class TempServlet extends HttpServlet {
		private static final long serialVersionUID = -6875852203346145792L;

		@Override
		public void init() throws ServletException {
			super.init();
		}

		@Override
		public void init(ServletConfig config) throws ServletException {
			super.init(config);
		}

		public void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws IOException {
			PrintWriter out = resp.getWriter();
			out.println("<html><body>Hello, <b>Eric</b>.</body></html>");
		}
	}

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

		servletContextHandler.addServlet(TempServlet.class, "/test");

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
