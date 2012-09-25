package com.linuxbox.enkive.imap.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MailboxConstants;
import org.apache.james.mailbox.model.MessageMetaData;
import org.apache.james.mailbox.model.MessageRange;
import org.apache.james.mailbox.model.MessageRange.Type;
import org.apache.james.mailbox.store.SimpleMessageMetaData;
import org.apache.james.mailbox.store.mail.AbstractMessageMapper;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.apache.james.mailbox.store.mail.model.Message;

import com.linuxbox.enkive.imap.EnkiveImapStore;
import com.linuxbox.enkive.retriever.MessageRetrieverService;
import com.linuxbox.util.PreviousItemRemovingIterator;

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
			MessageRange set, FetchType fType, int max) throws MailboxException {
		final List<Message<String>> results;
		final long from = set.getUidFrom();
		final long to = set.getUidTo();
		final Type type = set.getType();

		if (mailbox.getName().equals(MailboxConstants.INBOX)) {
			results = new ArrayList<Message<String>>();
			if (from <= 1)
				results.add(new EnkiveImapTemplateMessage());
		} else {

			switch (type) {
			default:
			case ALL:
				results = findMessagesInMailboxBetweenUIDs(mailbox, 0, -1, max);
				break;
			case FROM:
				results = findMessagesInMailboxBetweenUIDs(mailbox, from, -1,
						max);
				break;
			case ONE:
				results = findMessagesInMailboxBetweenUIDs(mailbox, from, from,
						max);
				break;
			case RANGE:
				results = findMessagesInMailboxBetweenUIDs(mailbox, from, to,
						max);
				break;
			}
		}
		return new PreviousItemRemovingIterator<Message<String>>(
				results.iterator());

	}

	private List<Message<String>> findMessagesInMailboxBetweenUIDs(
			Mailbox<String> mailbox, long from, long to, int max)
			throws MailboxException {
		int cur = 0;
		SortedMap<Long, String> uidMap = null;
		uidMap = getMailboxMessageIds(mailbox, from, to);

		ArrayList<Message<String>> messages = new ArrayList<Message<String>>();
		for (Entry<Long, String> entry : uidMap.entrySet()) {
			EnkiveImapMessage enkiveImapMessage = new EnkiveImapMessage(
					entry.getValue(), retrieverService);
			enkiveImapMessage.setUid(entry.getKey());
			messages.add(enkiveImapMessage);
			if (max != -1) {
				cur++;
				if (cur >= max)
					break;
			}
		}
		return messages;

	}

	@Override
	public Map<Long, MessageMetaData> expungeMarkedForDeletionInMailbox(
			Mailbox<String> mailbox, MessageRange set) throws MailboxException {
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
		return null;
	}

	@Override
	protected MessageMetaData copy(Mailbox<String> mailbox, long uid,
			long modSeq, Message<String> original) throws MailboxException {
		return new SimpleMessageMetaData(original);
	}

	@Override
	protected void begin() throws MailboxException {

	}

	@Override
	protected void commit() throws MailboxException {

	}

	@Override
	protected void rollback() throws MailboxException {

	}

	public abstract SortedMap<Long, String> getMailboxMessageIds(
			Mailbox<String> mailbox, long fromUid, long toUid);

}
