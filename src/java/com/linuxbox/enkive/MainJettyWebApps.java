/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
		Server server = context.getBean("Server",
				org.eclipse.jetty.server.Server.class);

		super.doEventLoop(context);
		
		try {
			server.stop();
			System.exit(0);
		} catch (Exception e) {
			LOGGER.error("Error stopping Jetty server.", e);
			System.exit(1);
		}
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
