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
package com.linuxbox.enkive.imap.mailbox;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.model.MessageMetaData;
import org.apache.james.mailbox.model.MessageRange;
import org.apache.james.mailbox.store.MailboxEventDispatcher;
import org.apache.james.mailbox.store.MailboxSessionMapperFactory;
import org.apache.james.mailbox.store.SimpleMessageMetaData;
import org.apache.james.mailbox.store.mail.MessageMapper;
import org.apache.james.mailbox.store.mail.MessageMapper.FetchType;
import org.apache.james.mailbox.store.mail.model.Message;
import org.apache.james.mailbox.store.mail.model.impl.SimpleMailbox;

import com.linuxbox.enkive.imap.mailbox.mongo.MongoEnkiveMailboxSessionMapperFactory;
import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;
import com.linuxbox.util.spring.ApplicationContextProvider;

public class EnkiveImapMailbox extends SimpleMailbox<String> {

	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.imap");

	private MailboxEventDispatcher<String> dispatcher;
	private Timer timer;
	private SearchQuery query;
	private MessageSearchService searchService;
	private EnkiveMailboxSession session;
	private long searchUpdateInterval;
	private EnkiveImapMailboxUpdater updater = null;
	private MessageMapper<String> mapper;

	public EnkiveImapMailbox(MailboxPath path, SearchQuery query,
			EnkiveMailboxSession session) throws MailboxException {
		super(path, query.getUIDValidity());
		this.session = session;
		this.dispatcher = session.getEventDispatcher();
		this.query = query;
		this.timer = new Timer();
		searchService = ApplicationContextProvider.getApplicationContext().
				getBean("SystemMessageSearchService", MessageSearchService.class);
		MailboxSessionMapperFactory<String> factory =
				ApplicationContextProvider.getApplicationContext().
				getBean("MongoEnkiveMailboxSessionMapperFactory",
						MongoEnkiveMailboxSessionMapperFactory.class);
		mapper = factory.getMessageMapper(session);
	}

	public void setSearchService(MessageSearchService searchService) {
		this.searchService = searchService;
	}

	public MessageSearchService getSearchService() {
		return searchService;
	}

	public void setSearchUpdateInterval(long searchUpdateInterval) {
		this.searchUpdateInterval = searchUpdateInterval;
		if (this.updater != null) {
			this.updater.cancel();
		}

		this.updater = new EnkiveImapMailboxUpdater(this);
		timer.schedule(this.updater, this.searchUpdateInterval * 1000,
				this.searchUpdateInterval * 1000);
	}

	public long getSearchUpdateInterval() {
		return searchUpdateInterval;
	}

	public SearchQuery getQuery() {
		return query;
	}

	public void setQuery(SearchQuery query) {
		this.query = query;
	}

	public SearchResult getResult() {
		if (query == null) {
			return null;
		}
		return query.getResult();
	}

	public SortedMap<Long, MessageMetaData> update() {
		if (query == null) {
			return (null);
		}

		Long nextUID = query.getResult().getNextUID();
		TreeMap<Long, MessageMetaData> newMsgs = new TreeMap<Long, MessageMetaData>();
		try {
			searchService.updateSearch(query);
		} catch (MessageSearchException e) {
			return (null);
		}

		Long lastUID = query.getResult().getNextUID();
		if (nextUID == lastUID) {
			return (null);
		}

        Iterator<Message<String>> messages;
		try {
			messages = mapper.findInMailbox(this,
					MessageRange.range(nextUID, lastUID), FetchType.Full, -1);
		} catch (MailboxException e) {
			return (null);
		}
        while(messages.hasNext()) {
		Message<String> message = messages.next();
		newMsgs.put(message.getUid(), new SimpleMessageMetaData(message));
        }

		return (newMsgs);
	}

	public void close() {
		timer.cancel();
	}

	private class EnkiveImapMailboxUpdater extends TimerTask {

		private EnkiveImapMailbox mailbox;

		EnkiveImapMailboxUpdater(EnkiveImapMailbox mailbox) {
			this.mailbox = mailbox;
		}
		@Override
		public void run() {
			LOGGER.trace("Updating " + mailbox.getName());
			SortedMap<Long, MessageMetaData> newMsgs = update();
			if (newMsgs != null) {
				LOGGER.trace("        Dispatch");
				dispatcher.added(session, newMsgs, mailbox);
			}
		}
	}
}
