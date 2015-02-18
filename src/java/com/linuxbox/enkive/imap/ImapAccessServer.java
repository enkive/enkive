/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.imapserver.netty.IMAPServer;

public class ImapAccessServer {

	IMAPServer imapServer;
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.imap");

	public IMAPServer getImapServer() {
		return imapServer;
	}

	public void setImapServer(IMAPServer imapServer) {
		this.imapServer = imapServer;
	}

	// Convenience method for Spring
	public void startup() {
		try {
			imapServer.init();
		} catch (Exception e) {
			LOGGER.error("Error initializing IMAP server", e);
		}
	}

	public void shutdown() {
		imapServer.unbind();
	}

}
