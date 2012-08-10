package com.linuxbox.enkive.imap;

import org.apache.james.imap.api.ImapMessage;
import org.apache.james.imap.api.process.ImapProcessor;
import org.apache.james.imap.api.process.ImapSession;

public class EnkiveImapProcessor implements ImapProcessor {

	@Override
	public void process(ImapMessage message, Responder responder,
			ImapSession session) {
		responder.respond(new EnkiveImapResponseMessage());

	}

}
