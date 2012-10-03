/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
package com.linuxbox.enkive.imap.mailbox.mongo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.SubscriptionException;

import com.linuxbox.enkive.imap.mailbox.EnkiveSubscriptionManager;
import com.linuxbox.enkive.imap.mongo.MongoEnkiveImapConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoEnkiveSubscriptionManager extends EnkiveSubscriptionManager {

	Mongo m;
	DB imapDB;
	DBCollection imapCollection;

	public MongoEnkiveSubscriptionManager(Mongo m, String imapDBName,
			String imapCollname) {
		this.m = m;
		imapDB = m.getDB(imapDBName);
		imapCollection = imapDB.getCollection(imapCollname);
	}

	/**
	 * Overridden method that returns all the mailboxes for a user.
	 */
	@Override
	public Collection<String> subscriptions(MailboxSession session)
			throws SubscriptionException {
		Collection<String> subscriptions = new HashSet<String>();

		DBObject searchObject = new BasicDBObject(
				MongoEnkiveImapConstants.USER, session.getUser().getUserName());
		DBObject userMailboxObject = imapCollection.findOne(searchObject);
		Map<String, String> mailboxes = (Map<String, String>) userMailboxObject
				.get(MongoEnkiveImapConstants.MAILBOXES);
		for (String mailboxName : mailboxes.keySet())
			subscriptions.add(mailboxName);

		return subscriptions;
	}

}
