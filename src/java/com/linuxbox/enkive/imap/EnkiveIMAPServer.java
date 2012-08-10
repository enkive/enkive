package com.linuxbox.enkive.imap;

import org.apache.james.imapserver.netty.IMAPServer;

public class EnkiveIMAPServer extends IMAPServer {

	@Override
	public int getDefaultPort() {
        return 5000;
    }
	
}
