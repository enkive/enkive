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
	    config = new XMLConfiguration(ClassLoader.getSystemResource(configurationPath));
	}

	//Convenience method for use with Spring
	public void startup() throws ConfigurationException {
			configure(config);
	}
}
