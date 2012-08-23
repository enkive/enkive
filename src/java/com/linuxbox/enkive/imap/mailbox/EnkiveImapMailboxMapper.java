package com.linuxbox.enkive.imap.mailbox;

import java.util.List;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.MailboxNotFoundException;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.store.mail.MailboxMapper;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.apache.james.mailbox.store.transaction.NonTransactionalMapper;

public abstract class EnkiveImapMailboxMapper extends NonTransactionalMapper
		implements MailboxMapper<String> {

	protected MailboxSession session;

	public EnkiveImapMailboxMapper(MailboxSession session) {
		this.session = session;
	}

	@Override
	public void endRequest() {
		// TODO Auto-generated method stub

	}

	@Override
	public void save(Mailbox<String> mailbox) throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Mailbox<String> mailbox) throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public abstract Mailbox<String> findMailboxByPath(MailboxPath mailboxName)
			throws MailboxException, MailboxNotFoundException;

	@Override
	public abstract List<Mailbox<String>> findMailboxWithPathLike(
			MailboxPath mailboxPath);

	@Override
	public abstract boolean hasChildren(Mailbox<String> mailbox, char delimiter)
			throws MailboxException, MailboxNotFoundException;

	@Override
	public abstract List<Mailbox<String>> list() throws MailboxException;

}
