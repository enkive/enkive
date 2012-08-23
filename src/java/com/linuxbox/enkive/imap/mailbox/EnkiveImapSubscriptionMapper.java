package com.linuxbox.enkive.imap.mailbox;

import java.util.List;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.SubscriptionException;
import org.apache.james.mailbox.store.user.SubscriptionMapper;
import org.apache.james.mailbox.store.user.model.Subscription;

public class EnkiveImapSubscriptionMapper implements SubscriptionMapper {

	MailboxSession session;
	
	public EnkiveImapSubscriptionMapper(MailboxSession session){
		this.session = session;
	}
	
	@Override
	public void endRequest() {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T execute(Transaction<T> transaction) throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Subscription findMailboxSubscriptionForUser(String user,
			String mailbox) throws SubscriptionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(Subscription subscription) throws SubscriptionException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Subscription> findSubscriptionsForUser(String user)
			throws SubscriptionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Subscription subscription) throws SubscriptionException {
		// TODO Auto-generated method stub

	}

}
