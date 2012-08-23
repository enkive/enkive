package com.linuxbox.enkive.imap.mailbox;

import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.store.mail.model.impl.SimpleMailbox;

public class EnkiveImapMailbox extends SimpleMailbox<String> {

	public EnkiveImapMailbox(MailboxPath path, long uidValidity) {
		super(path, uidValidity);
	}
}
