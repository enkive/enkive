package com.linuxbox.enkive.imap.mailbox;

import java.util.ArrayList;
import java.util.List;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.SubscriptionException;
import org.apache.james.mailbox.store.user.SubscriptionMapper;
import org.apache.james.mailbox.store.user.model.Subscription;

/**
 * Implementation of SubscriptionMapper Since Enkive IMAP access is designed as
 * read-only, unimplemented methods are designed to fail silently
 * 
 * @author lee
 * 
 */
public class EnkiveImapSubscriptionMapper implements SubscriptionMapper {

	MailboxSession session;

	public EnkiveImapSubscriptionMapper(MailboxSession session) {
		this.session = session;
	}

	@Override
	public void endRequest() {

	}

	@Override
	public <T> T execute(Transaction<T> transaction) throws MailboxException {
		throw new MailboxException("Subscriptions are not supported");
	}

	@Override
	public Subscription findMailboxSubscriptionForUser(String user,
			String mailbox) throws SubscriptionException {
		throw new SubscriptionException();
	}

	@Override
	public void save(Subscription subscription) throws SubscriptionException {

	}

	@Override
	public List<Subscription> findSubscriptionsForUser(String user)
			throws SubscriptionException {
		// Return empty list, since this action is unsupported
		return new ArrayList<Subscription>();
	}

	@Override
	public void delete(Subscription subscription) throws SubscriptionException {

	}

}
