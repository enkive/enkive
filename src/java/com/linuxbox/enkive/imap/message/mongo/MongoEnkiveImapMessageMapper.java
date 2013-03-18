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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MailboxConstants;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.imap.EnkiveImapStore;
import com.linuxbox.enkive.imap.message.EnkiveImapMessageMapper;
import com.linuxbox.enkive.imap.mongo.MongoEnkiveImapConstants;
import com.linuxbox.enkive.retriever.MessageRetrieverService;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoEnkiveImapMessageMapper extends EnkiveImapMessageMapper {
	Mongo m;
	DB imapDB;
	DBCollection imapCollection;

	public MongoEnkiveImapMessageMapper(MailboxSession mailboxSession,
			EnkiveImapStore store, MessageRetrieverService retrieverService,
			Mongo m, String enkiveDbName, String imapCollectionName) {
		super(mailboxSession, store, retrieverService);
		imapDB = m.getDB(enkiveDbName);
		imapCollection = imapDB.getCollection(imapCollectionName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long countMessagesInMailbox(Mailbox<String> mailbox)
			throws MailboxException {
		long messageCount = 0;
		if (mailbox.getName().equals(MailboxConstants.INBOX))
			messageCount = 1;
		else if (mailbox.getMailboxId() != null) {
			BasicDBObject mailboxQueryObject = new BasicDBObject();
			mailboxQueryObject.put("_id",
					ObjectId.massageToObjectId(mailbox.getMailboxId()));
			DBObject mailboxObject = imapCollection.findOne(mailboxQueryObject);

			Map<String, String> messageIds = null;
			Object messageIdsMap = mailboxObject
					.get(MongoEnkiveImapConstants.MESSAGEIDS);
			if (messageIdsMap instanceof Map) {
				messageIds = (Map<String, String>) messageIdsMap;
			}

			if (messageIds != null)
				messageCount = messageIds.size();
		}
		return messageCount;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SortedMap<Long, String> getMailboxMessageIds(
			Mailbox<String> mailbox, long fromUid, long toUid) {
		SortedMap<Long, String> messageIds = new TreeMap<Long, String>();

		if (mailbox.getMailboxId() != null) {
			BasicDBObject mailboxQueryObject = new BasicDBObject();
			mailboxQueryObject.put("_id",
					ObjectId.massageToObjectId(mailbox.getMailboxId()));
			DBObject mailboxObject = imapCollection.findOne(mailboxQueryObject);
			Object messageIdsMap = mailboxObject
					.get(MongoEnkiveImapConstants.MESSAGEIDS);

			Map<String, String> tmpMsgIds = null;
			if (messageIdsMap instanceof HashMap) {
				tmpMsgIds = (HashMap<String, String>) messageIdsMap;
			}

			if (tmpMsgIds == null || tmpMsgIds.isEmpty())
				return messageIds;

			if (fromUid > tmpMsgIds.size()) {
				// Do Nothing
			} else if (toUid < 0) {
				TreeSet<Long> sortedUids = new TreeSet<Long>();
				for (String key : tmpMsgIds.keySet())
					sortedUids.add(Long.parseLong(key));
				SortedSet<Long> sortedSubSet = sortedUids
						.tailSet(fromUid, true);
				for (Long key : sortedSubSet)
					messageIds.put(key, tmpMsgIds.get(key.toString()));
			} else if (fromUid == toUid) {
				messageIds.put(fromUid, tmpMsgIds.get(String.valueOf(fromUid)));
			} else {
				TreeSet<Long> sortedUids = new TreeSet<Long>();
				for (String key : tmpMsgIds.keySet())
					sortedUids.add(Long.parseLong(key));
				SortedSet<Long> sortedSubSet = sortedUids
						.subSet(fromUid, toUid);
				for (Long key : sortedSubSet)
					messageIds.put(key, tmpMsgIds.get(key.toString()));
			}
		}
		
		return messageIds;
	}
}
