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
package com.linuxbox.enkive.imap.mailbox.mongo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.SubscriptionException;

import com.linuxbox.enkive.imap.mailbox.EnkiveSubscriptionManager;
import com.linuxbox.enkive.imap.mongo.MongoEnkiveImapConstants;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoEnkiveSubscriptionManager extends EnkiveSubscriptionManager {

	DBCollection imapCollection;

	public MongoEnkiveSubscriptionManager(MongoClient m, String imapDBName,
			String imapCollname) {
		this(m.getDB(imapDBName).getCollection(imapCollname));
	}
	
	public MongoEnkiveSubscriptionManager(MongoDbInfo dbInfo) {
		this(dbInfo.getCollection());
	}

	public MongoEnkiveSubscriptionManager(DBCollection imapCollection) {
		this.imapCollection = imapCollection;
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

		@SuppressWarnings("unchecked")
		Map<String, String> mailboxes = (Map<String, String>) userMailboxObject
				.get(MongoEnkiveImapConstants.MAILBOXES);

		subscriptions.addAll(mailboxes.keySet());

		return subscriptions;
	}
}
