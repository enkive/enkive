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
package com.linuxbox.enkive.imap.message.mongo;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MailboxConstants;
import org.apache.james.mailbox.store.mail.model.Mailbox;

import com.linuxbox.enkive.imap.EnkiveImapStore;
import com.linuxbox.enkive.imap.mailbox.EnkiveImapMailbox;
import com.linuxbox.enkive.imap.message.EnkiveImapMessageMapper;
import com.linuxbox.enkive.retriever.MessageRetrieverService;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;

public class MongoEnkiveImapMessageMapper extends EnkiveImapMessageMapper {

	public MongoEnkiveImapMessageMapper(MailboxSession mailboxSession,
			EnkiveImapStore store, MessageRetrieverService retrieverService) {
		super(mailboxSession, store, retrieverService);
	}

	@Override
	public long countMessagesInMailbox(Mailbox<String> mailbox)
			throws MailboxException {
		long messageCount = 0;
		if (mailbox.getName().equals(MailboxConstants.INBOX))
			messageCount = 1;
		else if (mailbox.getName().equals("Trash"))
			messageCount = 0;
		else if (mailbox.getName() != null) {
			EnkiveImapMailbox ebox = (EnkiveImapMailbox)mailbox;
			SearchResult result = ebox.getResult();
			if (result != null) {
				messageCount = result.getMessageIds().size();
			}
		}
		if (LOGGER.isInfoEnabled())
			LOGGER.info("countMessagesInMailbox " + messageCount);

		return messageCount;
	}

	@Override
	public SortedMap<Long, String> getMailboxMessageIds(
			Mailbox<String> mailbox, long fromUid, long toUid) {
		SortedMap<Long, String> messageIds = new TreeMap<Long, String>();

		if (LOGGER.isInfoEnabled())
			LOGGER.info("getMailboxMessageIds begin " + fromUid + " " + toUid);

		if (mailbox.getName() == null) {
			return messageIds;
		}

		if (!(mailbox instanceof EnkiveImapMailbox)) {
			return messageIds;
		}

		SearchResult result = ((EnkiveImapMailbox)mailbox).getResult();
		if (result == null) {
			return messageIds;
		}

		Map<Long, String> msgIds = result.getMessageIds();
		LOGGER.trace("   msgIds " + msgIds.size());
		if (fromUid >= msgIds.size()) {
			// Requested start is beyond the end
			return messageIds;
		}

		if (fromUid == toUid && toUid != -1) {
			// Only one requested
			messageIds.put(fromUid, msgIds.get(fromUid));
			return messageIds;
		}

		TreeSet<Long> sortedUids = new TreeSet<Long>(msgIds.keySet());

		SortedSet<Long> sortedSubSet;
		if (toUid == -1) {
			sortedSubSet = sortedUids.tailSet(fromUid, true);
		} else {
			// Given range is inclusive, subSet is exclusive w.r.t. end
			sortedSubSet = sortedUids.subSet(fromUid, toUid + 1);
		}

		for (Long key : sortedSubSet) {
			messageIds.put(key, msgIds.get(key));
		}
		
		if (LOGGER.isInfoEnabled())
			LOGGER.info("getMailboxMessageIds end " + messageIds.size());

		return messageIds;
	}
}
