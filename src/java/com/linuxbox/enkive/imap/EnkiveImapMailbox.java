package com.linuxbox.enkive.imap;

import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.store.mail.model.impl.SimpleMailbox;

public class EnkiveImapMailbox extends SimpleMailbox<Long> {

	public EnkiveImapMailbox(MailboxPath path, long uidValidity) {
		super(path, uidValidity);
		// TODO Auto-generated constructor stub
	}

}
