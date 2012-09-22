package com.linuxbox.enkive.imap.message;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.james.mailbox.store.mail.model.Message;

public class EnkiveImapMessageIterator implements Iterator<Message<String>> {

	ArrayList<EnkiveImapMessage> messages;
	int i = -1;

	public EnkiveImapMessageIterator(ArrayList<EnkiveImapMessage> messages) {
		this.messages = messages;
	}

	@Override
	public boolean hasNext() {
		return (i < messages.size() - 1);
	}

	@Override
	public EnkiveImapMessage next() {
		if (hasNext()) {
			i++;
			EnkiveImapMessage message = messages.get(i);
			remove();
			return message;
		} else
			return null;
	}

	@Override
	public void remove() {
		if (i > 0) {
			EnkiveImapMessage previousMessage = messages.get(i - 1);
			previousMessage.setMessage(null);
		}
	}

}
