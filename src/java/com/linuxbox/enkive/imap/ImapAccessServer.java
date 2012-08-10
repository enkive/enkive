package com.linuxbox.enkive.imap;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.james.imap.decode.main.DefaultImapDecoder;
import org.apache.james.imap.decode.parser.ImapParserFactory;
import org.apache.james.imap.encode.base.EndImapEncoder;
import org.apache.james.imap.message.response.UnpooledStatusResponseFactory;
import org.slf4j.Logger;

public class ImapAccessServer {

	public ImapAccessServer() {
		EnkiveIMAPServer imapServer = new EnkiveIMAPServer();
		Logger logger = new org.slf4j.impl.Log4jLoggerFactory()
				.getLogger("com.linuxbox.enkive.imap");
		imapServer.setLog(logger);
		imapServer.setImapDecoder(new DefaultImapDecoder(
				new UnpooledStatusResponseFactory(), new ImapParserFactory(
						new UnpooledStatusResponseFactory())));
		imapServer.setImapEncoder(new EnkiveImapEncoder());
		imapServer.setImapProcessor(new EnkiveImapProcessor());

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
