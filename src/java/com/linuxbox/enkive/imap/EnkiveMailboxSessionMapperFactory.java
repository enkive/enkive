package com.linuxbox.enkive.imap;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.SubscriptionException;
import org.apache.james.mailbox.store.MailboxSessionMapperFactory;
import org.apache.james.mailbox.store.mail.MailboxMapper;
import org.apache.james.mailbox.store.mail.MessageMapper;
import org.apache.james.mailbox.store.user.SubscriptionMapper;

public class EnkiveMailboxSessionMapperFactory extends
		MailboxSessionMapperFactory<Long> {

	@Override
	protected MessageMapper<Long> createMessageMapper(MailboxSession session)
			throws MailboxException {
		return new EnkiveImapMessageMapper(session, new EnkiveImapStore());
	}

	@Override
	protected MailboxMapper<Long> createMailboxMapper(MailboxSession session)
			throws MailboxException {
		return new EnkiveImapMailboxMapper(session);
	}

	@Override
	protected SubscriptionMapper createSubscriptionMapper(MailboxSession session)
			throws SubscriptionException {
		return new EnkiveImapSubscriptionMapper(session);
	}

}
