package com.linuxbox.enkive.imap;

import java.util.Collection;
import java.util.HashSet;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.SubscriptionManager;
import org.apache.james.mailbox.exception.SubscriptionException;

public class EnkiveSubscriptionManager implements SubscriptionManager {

	@Override
	public void startProcessingRequest(MailboxSession session) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endProcessingRequest(MailboxSession session) {
		// TODO Auto-generated method stub

	}

	@Override
	public void subscribe(MailboxSession session, String mailbox)
			throws SubscriptionException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<String> subscriptions(MailboxSession session)
			throws SubscriptionException {
		Collection<String> subscriptions = new HashSet<String>();
		// TODO Auto-generated method stub
		return subscriptions;
	}

	@Override
	public void unsubscribe(MailboxSession session, String mailbox)
			throws SubscriptionException {
		// TODO Auto-generated method stub

	}

}
