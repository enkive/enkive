package com.linuxbox.enkive.imap;

import java.io.IOException;

import org.apache.james.imap.api.ImapCommand;
import org.apache.james.imap.api.ImapMessage;
import org.apache.james.imap.api.process.ImapSession;
import org.apache.james.imap.encode.ImapEncoder;
import org.apache.james.imap.encode.ImapResponseComposer;

public class EnkiveImapEncoder implements ImapEncoder {

	@Override
	public void encode(ImapMessage message, ImapResponseComposer composer,
			ImapSession session) throws IOException {

		composer.commandResponse(ImapCommand.anyStateCommand("CAPABILITY"),
				"IMAP4rev1");

		composer.commandResponse(ImapCommand.nonAuthenticatedStateCommand("LOGIN"), "Logged in");
		composer.tag("OK");
		composer.end();

	}

}
