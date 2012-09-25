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
