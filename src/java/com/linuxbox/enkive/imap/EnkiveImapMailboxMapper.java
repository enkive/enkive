package com.linuxbox.enkive.imap;

import java.util.ArrayList;
import java.util.List;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.MailboxNotFoundException;
import org.apache.james.mailbox.model.MailboxConstants;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.store.mail.MailboxMapper;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.apache.james.mailbox.store.transaction.NonTransactionalMapper;

public class EnkiveImapMailboxMapper extends NonTransactionalMapper implements
		MailboxMapper<Long> {

	MailboxSession session;

	public EnkiveImapMailboxMapper(MailboxSession session) {
		this.session = session;
	}

	@Override
	public void endRequest() {
		// TODO Auto-generated method stub

	}

	@Override
	public void save(Mailbox<Long> mailbox) throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Mailbox<Long> mailbox) throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public Mailbox<Long> findMailboxByPath(MailboxPath mailboxName)
			throws MailboxException, MailboxNotFoundException {
		// TODO Auto-generated method stub
		System.out.println("find by path");
		return new EnkiveImapMailbox(mailboxName, 1);
	}

	@Override
	public List<Mailbox<Long>> findMailboxWithPathLike(MailboxPath mailboxPath)
			throws MailboxException {
		// TODO Auto-generated method stub
		return list();
	}

	@Override
	public boolean hasChildren(Mailbox<Long> mailbox, char delimiter)
			throws MailboxException, MailboxNotFoundException {
		// TODO Auto-generated method stub
		System.out.println("has children");
		return false;
	}

	@Override
	public List<Mailbox<Long>> list() throws MailboxException {
		System.out.println("get list");
		MailboxPath inboxPath = new MailboxPath(session.getPersonalSpace(), "Lee", MailboxConstants.INBOX);
		MailboxPath testPath = new MailboxPath(session.getPersonalSpace(), "Lee", "TESTFOLDER");
		ArrayList<Mailbox<Long>> mailboxes = new ArrayList<Mailbox<Long>>();
		mailboxes.add(new EnkiveImapMailbox(inboxPath, 1));
		mailboxes.add(new EnkiveImapMailbox(testPath, 2));
		return mailboxes;
	}
}
