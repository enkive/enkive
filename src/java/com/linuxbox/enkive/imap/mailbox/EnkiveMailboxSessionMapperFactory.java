package com.linuxbox.enkive.imap.mailbox;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.SubscriptionException;
import org.apache.james.mailbox.store.MailboxSessionMapperFactory;
import org.apache.james.mailbox.store.mail.MailboxMapper;
import org.apache.james.mailbox.store.mail.MessageMapper;
import org.apache.james.mailbox.store.user.SubscriptionMapper;

import com.linuxbox.enkive.retriever.MessageRetrieverService;

public abstract class EnkiveMailboxSessionMapperFactory extends
		MailboxSessionMapperFactory<String> {

	protected MessageRetrieverService retrieverService;

	@Override
	protected abstract MessageMapper<String> createMessageMapper(
			MailboxSession session) throws MailboxException;

	@Override
	protected abstract MailboxMapper<String> createMailboxMapper(
			MailboxSession session) throws MailboxException;

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
