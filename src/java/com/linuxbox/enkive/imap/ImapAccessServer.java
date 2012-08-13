package com.linuxbox.enkive.imap;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.james.imap.decode.ImapDecoderFactory;
import org.apache.james.imap.encode.ImapEncoderFactory;
import org.apache.james.imap.encode.main.DefaultImapEncoderFactory;
import org.apache.james.imap.main.DefaultImapDecoderFactory;
import org.apache.james.imap.processor.main.DefaultImapProcessorFactory;
import org.apache.james.mailbox.MailboxManager;
import org.slf4j.Logger;

public class ImapAccessServer {

	public ImapAccessServer() {

		MailboxManager mm = new EnkiveMailboxManager();

		ImapDecoderFactory df = new DefaultImapDecoderFactory();
		ImapEncoderFactory ef = new DefaultImapEncoderFactory();

		DefaultImapProcessorFactory pf = new DefaultImapProcessorFactory();
		pf.setMailboxManager(mm);
		pf.setSubscriptionManager(new EnkiveSubscriptionManager());

		EnkiveIMAPServer imapServer = new EnkiveIMAPServer();
		Logger logger = new org.slf4j.impl.Log4jLoggerFactory()
				.getLogger("com.linuxbox.enkive.imap");
		imapServer.setLog(logger);
		imapServer.setImapDecoder(df.buildImapDecoder());
		imapServer.setImapEncoder(ef.buildImapEncoder());
		imapServer.setImapProcessor(pf.buildImapProcessor());

		HierarchicalConfiguration config = new HierarchicalConfiguration();
		config.addProperty("connectionLimit", "50");
		try {

			imapServer.configure(config);

			imapServer.bind();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ImapAccessServer server = new ImapAccessServer();
	}

}
