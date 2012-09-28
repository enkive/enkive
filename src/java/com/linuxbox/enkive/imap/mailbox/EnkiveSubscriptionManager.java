package com.linuxbox.enkive.imap.mailbox;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.SubscriptionManager;
import org.apache.james.mailbox.exception.SubscriptionException;

/**
 * Abstract implementation of SubscriptionManager
 * 
 * Since Enkive is designed as read-only, unimplemented methods fail silently
 * 
 * @author lee
 * 
 */
public abstract class EnkiveSubscriptionManager implements SubscriptionManager {

	@Override
	public void startProcessingRequest(MailboxSession session) {

	}

	@Override
	public void endProcessingRequest(MailboxSession session) {

	}

	@Override
	public void subscribe(MailboxSession session, String mailbox)
			throws SubscriptionException {

	}

	@Override
	public void unsubscribe(MailboxSession session, String mailbox)
			throws SubscriptionException {

	}

}
