package com.linuxbox.enkive.imap;

import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MessageResult;
import org.apache.james.mailbox.model.MessageResultIterator;

public class EnkiveMessageResultIterator implements MessageResultIterator {

	boolean next = true;
	
	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return next;
	}

	@Override
	public MessageResult next() {
		MessageResult result = new EnkiveMessageResult();
		// TODO Auto-generated method stub
		next = false;
		return result;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

	@Override
	public MailboxException getException() {
		// TODO Auto-generated method stub
		return null;
	}

}
