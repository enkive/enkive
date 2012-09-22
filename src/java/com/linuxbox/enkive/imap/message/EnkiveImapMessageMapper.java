package com.linuxbox.enkive.imap.message;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.linuxbox.enkive.imap.EnkiveImapStore;
import com.linuxbox.enkive.retriever.MessageRetrieverService;

public abstract class EnkiveImapMessageMapper extends
		AbstractMessageMapper<String> {

	MessageRetrieverService retrieverService;

	public EnkiveImapMessageMapper(MailboxSession mailboxSession,
			EnkiveImapStore store, MessageRetrieverService retrieverService) {
		super(mailboxSession, store, store);
		this.retrieverService = retrieverService;
	}

	@Override
	public Iterator<Message<String>> findInMailbox(Mailbox<String> mailbox,
			MessageRange set,
			org.apache.james.mailbox.store.mail.MessageMapper.FetchType type,
			int limit) throws MailboxException {
		System.out.println(set);
		ArrayList<EnkiveImapMessage> messages = new ArrayList<EnkiveImapMessage>();
		final long from = set.getUidFrom();
		final long to = set.getUidTo();

		Map<Long, String> messageIds = getMailboxMessageIds(mailbox, from, to,
				limit);

		for (Long messageUid : messageIds.keySet()) {
			EnkiveImapMessage enkiveImapMessage = new EnkiveImapMessage(
					messageIds.get(messageUid), retrieverService);
			enkiveImapMessage.setUid(messageUid.longValue());
			messages.add(enkiveImapMessage);
		}

		return new EnkiveImapMessageIterator(messages);
	}

	@Override
	public Map<Long, MessageMetaData> expungeMarkedForDeletionInMailbox(
			Mailbox<String> mailbox, MessageRange set) throws MailboxException {
		// TODO Auto-generated method stub
		return new HashMap<Long, MessageMetaData>();
	}

	@Override
	public abstract long countMessagesInMailbox(Mailbox<String> mailbox)
			throws MailboxException;

	@Override
	public long countUnseenMessagesInMailbox(Mailbox<String> mailbox)
			throws MailboxException {
		return 0;
	}

	@Override
	public void delete(Mailbox<String> mailbox, Message<String> message)
			throws MailboxException {
		// Unsupported method

	}

	@Override
	public Long findFirstUnseenMessageUid(Mailbox<String> mailbox)
			throws MailboxException {
		return null;
	}

	@Override
	public List<Long> findRecentMessageUidsInMailbox(Mailbox<String> mailbox)
			throws MailboxException {
		ArrayList<Long> messageIds = new ArrayList<Long>();
		return messageIds;
	}

	@Override
	public void endRequest() {

	}

	@Override
	protected MessageMetaData save(Mailbox<String> mailbox,
			Message<String> message) throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected MessageMetaData copy(Mailbox<String> mailbox, long uid,
			long modSeq, Message<String> original) throws MailboxException {
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

	public abstract Map<Long, String> getMailboxMessageIds(
			Mailbox<String> mailbox, long fromUid, long toUid, int limit);

}
