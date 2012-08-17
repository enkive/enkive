package com.linuxbox.enkive.imap;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.SubscriptionException;
import org.apache.james.mailbox.store.MailboxSessionMapperFactory;
import org.apache.james.mailbox.store.mail.MailboxMapper;
import org.apache.james.mailbox.store.mail.MessageMapper;
import org.apache.james.mailbox.store.user.SubscriptionMapper;

import com.linuxbox.enkive.retriever.MessageRetrieverService;

public class EnkiveMailboxSessionMapperFactory extends
		MailboxSessionMapperFactory<Long> {

	MessageRetrieverService retrieverService;
	
	@Override
	protected MessageMapper<Long> createMessageMapper(MailboxSession session)
			throws MailboxException {
		return new EnkiveImapMessageMapper(session, new EnkiveImapStore(), retrieverService);
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

	public MessageRetrieverService getRetrieverService() {
		return retrieverService;
	}

	public void setRetrieverService(MessageRetrieverService retrieverService) {
		this.retrieverService = retrieverService;
	}

}
