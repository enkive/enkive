package com.linuxbox.enkive.imap;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.james.container.spring.filesystem.ResourceLoaderFileSystem;
import org.apache.james.imap.decode.ImapDecoderFactory;
import org.apache.james.imap.encode.ImapEncoderFactory;
import org.apache.james.imap.encode.main.DefaultImapEncoderFactory;
import org.apache.james.imap.main.DefaultImapDecoderFactory;
import org.apache.james.imap.processor.main.DefaultImapProcessorFactory;
import org.slf4j.Logger;

public class ImapAccessServer {

	EnkiveIMAPServer imapServer;
	
	public ImapAccessServer(EnkiveMailboxManager mm) {
	
		ResourceLoaderFileSystem filesystem = new ResourceLoaderFileSystem();
		filesystem.setResourceLoader(new EnkiveResourceLoader());
		
		ImapDecoderFactory df = new DefaultImapDecoderFactory();
		ImapEncoderFactory ef = new DefaultImapEncoderFactory();

		DefaultImapProcessorFactory pf = new DefaultImapProcessorFactory();
		pf.setMailboxManager(mm);
		pf.setSubscriptionManager(new EnkiveSubscriptionManager());

		imapServer = new EnkiveIMAPServer(5000);
		System.out.println(imapServer.getStartTLSSupported());
		Logger logger = new org.slf4j.impl.Log4jLoggerFactory()
				.getLogger("com.linuxbox.enkive.imap");
		imapServer.setLog(logger);
		imapServer.setImapDecoder(df.buildImapDecoder());
		imapServer.setImapEncoder(ef.buildImapEncoder());
		imapServer.setImapProcessor(pf.buildImapProcessor());
		imapServer.setFileSystem(filesystem);

		HierarchicalConfiguration config = new HierarchicalConfiguration();
		config.addProperty("connectionLimit", "50");
		config.addProperty("plainAuthDisallowed", true);
		config.addProperty("tls.[@startTLS]", true);
		//config.addProperty("tls.supportedCipherSuites.cipherSuite", true);
		config.addProperty("tls.keystore", "classpath:/imap/keystore");
		config.addProperty("tls.secret", "enkiveimap");
		
		try {

			imapServer.configure(config);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startup(){
		try {
			imapServer.init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void shutdown(){
		imapServer.unbind();
	}

}
