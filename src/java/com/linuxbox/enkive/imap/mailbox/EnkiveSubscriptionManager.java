package com.linuxbox.enkive.imap.mailbox;

import java.util.Collection;
import java.util.HashSet;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.SubscriptionManager;
import org.apache.james.mailbox.exception.SubscriptionException;

public class EnkiveSubscriptionManager implements SubscriptionManager {

	// This class is unsupported
	@Override
	public void startProcessingRequest(MailboxSession session) {

	}

	@Override
	public void endProcessingRequest(MailboxSession session) {

	}

	@Override
	public void subscribe(MailboxSession session, String mailbox)
			throws SubscriptionException {
		throw new SubscriptionException();

	}

	@Override
	public Collection<String> subscriptions(MailboxSession session)
			throws SubscriptionException {
		// Return an empty collection, since this action is unsupported
		Collection<String> subscriptions = new HashSet<String>();
		return subscriptions;
	}

	@Override
	public void unsubscribe(MailboxSession session, String mailbox)
			throws SubscriptionException {
		throw new SubscriptionException();
	}

}
