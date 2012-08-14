package com.linuxbox.enkive.imap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MessageMetaData;
import org.apache.james.mailbox.model.MessageRange;
import org.apache.james.mailbox.store.SimpleMessageMetaData;
import org.apache.james.mailbox.store.mail.AbstractMessageMapper;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.apache.james.mailbox.store.mail.model.Message;

public class EnkiveImapMessageMapper extends AbstractMessageMapper<Long> {

	public EnkiveImapMessageMapper(MailboxSession mailboxSession,
			EnkiveImapStore store) {
		super(mailboxSession, store, store);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Iterator<Message<Long>> findInMailbox(Mailbox<Long> mailbox,
			MessageRange set,
			org.apache.james.mailbox.store.mail.MessageMapper.FetchType type,
			int limit) throws MailboxException {
		ArrayList<Message<Long>> messages = new ArrayList<Message<Long>>();
		// TODO Auto-generated method stub
		if (set.getUidFrom() < 1) {
			messages.add(new EnkiveImapMessage());
			System.out.println("HERE");
		}
		return messages.iterator();
	}

	@Override
	public Map<Long, MessageMetaData> expungeMarkedForDeletionInMailbox(
			Mailbox<Long> mailbox, MessageRange set) throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long countMessagesInMailbox(Mailbox<Long> mailbox)
			throws MailboxException {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public long countUnseenMessagesInMailbox(Mailbox<Long> mailbox)
			throws MailboxException {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void delete(Mailbox<Long> mailbox, Message<Long> message)
			throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public Long findFirstUnseenMessageUid(Mailbox<Long> mailbox)
			throws MailboxException {
		// TODO Auto-generated method stub
		return (long) 1;
	}

	@Override
	public List<Long> findRecentMessageUidsInMailbox(Mailbox<Long> mailbox)
			throws MailboxException {
		// TODO Auto-generated method stub
		ArrayList<Long> messageIds = new ArrayList<Long>();
		messageIds.add((long) 1);
		return messageIds;
	}

	@Override
	public void endRequest() {
		// TODO Auto-generated method stub

	}

	@Override
	protected MessageMetaData save(Mailbox<Long> mailbox, Message<Long> message)
			throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected MessageMetaData copy(Mailbox<Long> mailbox, long uid,
			long modSeq, Message<Long> original) throws MailboxException {
		// TODO Auto-generated method stub
		return new SimpleMessageMetaData(original);
	}

	@Override
	protected void begin() throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void commit() throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void rollback() throws MailboxException {
		// TODO Auto-generated method stub

	}

}
