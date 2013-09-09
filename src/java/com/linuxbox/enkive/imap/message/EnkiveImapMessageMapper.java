/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.linuxbox.enkive.imap.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.imap");

	private MessageRetrieverService retrieverService;

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

		if (LOGGER.isInfoEnabled())
			LOGGER.info("findInMailbox " + mailbox.getName());

		if (mailbox.getName().equals(MailboxConstants.INBOX)) {
			results = new ArrayList<Message<String>>();
			if (from <= 1)
				results.add(new EnkiveImapTemplateMessage(
						"ImapInboxEmailTemplate.ftl"));
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
		return new PreviousItemRemovingIterator<Message<String>>(results.iterator());

	}

	private List<Message<String>> findMessagesInMailboxBetweenUIDs(
			Mailbox<String> mailbox, long from, long to, int max)
			throws MailboxException {
		int cur = 0;
		SortedMap<Long, String> uidMap = null;

		if (LOGGER.isInfoEnabled())
			LOGGER.info("findMessagesInMailboxBetweenUIDs " + mailbox.getName() + " " + from + " "  + to);

		uidMap = getMailboxMessageIds(mailbox, from, to);

		ArrayList<Message<String>> messages = new ArrayList<Message<String>>();
		for (Entry<Long, String> entry : uidMap.entrySet()) {
			EnkiveImapMessage enkiveImapMessage = new EnkiveImapMessage(
					entry.getValue(), retrieverService);
			if (enkiveImapMessage.messageExists()) {
				enkiveImapMessage.setUid(entry.getKey());
				messages.add(enkiveImapMessage);
				if (max != -1) {
					cur++;
					if (cur >= max)
						break;
				}
			}
		}
		return messages;

	}

	@Override
	public Map<Long, MessageMetaData> expungeMarkedForDeletionInMailbox(
			Mailbox<String> mailbox, MessageRange set) throws MailboxException {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("expungeMarkedForDeletionInMailbox");

		return new HashMap<Long, MessageMetaData>();
	}

	@Override
	public abstract long countMessagesInMailbox(Mailbox<String> mailbox)
			throws MailboxException;

	@Override
	public long countUnseenMessagesInMailbox(Mailbox<String> mailbox)
			throws MailboxException {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("countUnseenMessagesInMailbox");

		return 0;
	}

	@Override
	public void delete(Mailbox<String> mailbox, Message<String> message)
			throws MailboxException {
		// Unsupported method

		if (LOGGER.isInfoEnabled())
			LOGGER.info("delete");

	}

	@Override
	public Long findFirstUnseenMessageUid(Mailbox<String> mailbox)
			throws MailboxException {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("findFirstUnseenMessageUid");

		return null;
	}

	@Override
	public List<Long> findRecentMessageUidsInMailbox(Mailbox<String> mailbox)
			throws MailboxException {
		ArrayList<Long> messageIds = new ArrayList<Long>();
		if (LOGGER.isInfoEnabled())
			LOGGER.info("findRecentMessageUidsInMailbox");

		return messageIds;
	}

	@Override
	public void endRequest() {

		if (LOGGER.isInfoEnabled())
			LOGGER.info("endRequest");

	}

	@Override
	protected MessageMetaData save(Mailbox<String> mailbox,
			Message<String> message) throws MailboxException {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("save");

		return null;
	}

	@Override
	protected MessageMetaData copy(Mailbox<String> mailbox, long uid,
			long modSeq, Message<String> original) throws MailboxException {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("copy");

		return new SimpleMessageMetaData(original);
	}

	@Override
	public MessageMetaData move(Mailbox<String> mailbox, Message<String> message)
			throws MailboxException {
		// Unsupported method
		if (LOGGER.isInfoEnabled())
			LOGGER.info("move");

		return null;

	}

	@Override
	protected void begin() throws MailboxException {

		if (LOGGER.isInfoEnabled())
			LOGGER.info("begin");

	}

	@Override
	protected void commit() throws MailboxException {

		if (LOGGER.isInfoEnabled())
			LOGGER.info("commit");

	}

	@Override
	protected void rollback() throws MailboxException {

		if (LOGGER.isInfoEnabled())
			LOGGER.info("rollback");

	}

	public abstract SortedMap<Long, String> getMailboxMessageIds(
			Mailbox<String> mailbox, long fromUid, long toUid);

}
