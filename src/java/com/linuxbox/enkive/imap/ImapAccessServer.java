package com.linuxbox.enkive.imap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.imapserver.netty.IMAPServer;

public class ImapAccessServer {

	IMAPServer imapServer;
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.imap");

	public ImapAccessServer(IMAPServer imapServer) {
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
