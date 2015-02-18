/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.springframework.context.ApplicationContext;

import com.linuxbox.enkive.exception.UnimplementedMethodException;

public class EnkiveDaemon extends Main implements Daemon {
	private static final String DESCRIPTION = "com.linuxbox.enkive.Daemon";

	static final String[] CONFIG_FILES = { "jetty-server-webapps.xml" };

	public EnkiveDaemon() {
		super(NO_ARGS, CONFIG_FILES, DESCRIPTION);
	}

	@Override
	public void init(DaemonContext context) throws DaemonInitException,
			Exception {
		// empty
	}

	@Override
	public void destroy() {
		// empty
	}

	/**
	 * This should never be called as org.apache.commons.daemon.Daemon is
	 * calling start() and stop() on its own.
	 */
	@Override
	protected void runCoreFunctionality(ApplicationContext context) {
		throw new UnimplementedMethodException();
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
