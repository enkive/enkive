/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
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
 ******************************************************************************/
package com.linuxbox.enkive.imap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.james.imapserver.netty.IMAPServer;
import org.slf4j.Logger;

public class EnkiveIMAPServer extends IMAPServer {

	HierarchicalConfiguration config;

	public EnkiveIMAPServer(String configurationPath)
			throws ConfigurationException {

		Logger logger = new org.slf4j.impl.Log4jLoggerFactory()
				.getLogger("com.linuxbox.enkive.imap");
		setLog(logger);
		config = new XMLConfiguration(
				ClassLoader.getSystemResource(configurationPath));
	}

	// Convenience method for use with Spring
	public void startup() throws ConfigurationException {
		configure(config);
	}
}
