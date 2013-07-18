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
package com.linuxbox.enkive.imap.mongo;

import java.util.HashMap;
import java.util.TreeSet;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.imap.EnkiveImapStore;
import com.linuxbox.util.mongodb.MongoDbConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoEnkiveImapStore extends EnkiveImapStore {
	MongoClient m;
	DB imapDB;
	DBCollection imapCollection;

	public MongoEnkiveImapStore(MongoClient m, String imapDBName, String imapCollName) {
		this.m = m;
		imapDB = m.getDB(imapDBName);
		imapCollection = imapDB.getCollection(imapCollName);
	}

	@Override
	public long lastUid(MailboxSession session, Mailbox<String> mailbox)
			throws MailboxException {
		// Get user, get mailboxid from path, get number of messages
		BasicDBObject userMailboxesSearchObject = new BasicDBObject(
				MongoEnkiveImapConstants.USER, session.getUser().getUserName());
		DBObject userMailboxesObject = imapCollection
				.findOne(userMailboxesSearchObject);
		
		@SuppressWarnings("unchecked")
		HashMap<String, String> userMailboxes = (HashMap<String, String>) userMailboxesObject
				.get(MongoEnkiveImapConstants.MAILBOXES);
		
		String mailboxKey = userMailboxes.get(mailbox.getName());
		DBObject mailboxObject = imapCollection.findOne(new BasicDBObject(
					MongoDbConstants.OBJECT_ID_KEY,
					ObjectId.massageToObjectId(mailboxKey)));
		
		@SuppressWarnings("unchecked")
		HashMap<String, String> msgIds = (HashMap<String, String>) mailboxObject
				.get(MongoEnkiveImapConstants.MESSAGEIDS);
		
		if (msgIds == null || msgIds.keySet() == null
				|| msgIds.keySet().isEmpty()) {
			return 1;
		}
		TreeSet<Long> sortedUids = new TreeSet<Long>();
		for (String key : msgIds.keySet())
			sortedUids.add(Long.parseLong(key));
		
		return sortedUids.last();
	}

}
