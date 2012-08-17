package com.linuxbox.enkive.imap;

import org.apache.james.imapserver.netty.IMAPServer;

public class EnkiveIMAPServer extends IMAPServer {

	int defaultPort;
	
	public EnkiveIMAPServer(int defaultPort){
		this.defaultPort = defaultPort;
	}
	
	@Override
	public int getDefaultPort() {
        return defaultPort;
    }
	
}
