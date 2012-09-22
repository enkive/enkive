package com.linuxbox.enkive.imap.mongo;

import java.util.HashMap;
import java.util.TreeSet;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.imap.EnkiveImapStore;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoEnkiveImapStore extends EnkiveImapStore {

	Mongo m;
	DB imapDB;
	DBCollection imapCollection;

	public MongoEnkiveImapStore(Mongo m, String imapDBName, String imapCollName) {
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
		HashMap<String, String> userMailboxes = (HashMap<String, String>) userMailboxesObject
				.get(MongoEnkiveImapConstants.MAILBOXES);
		String mailboxKey = userMailboxes.get(mailbox.getName());
		DBObject mailboxObject = imapCollection.findOne(new BasicDBObject(
				"_id", ObjectId.massageToObjectId(mailboxKey)));
		HashMap<String, String> msgIds = (HashMap<String, String>) mailboxObject
				.get(MongoEnkiveImapConstants.MESSAGEIDS);
		if (msgIds == null || msgIds.keySet() == null
				|| msgIds.keySet().isEmpty())
			return 1;
		TreeSet<Long> sortedUids = new TreeSet<Long>();
		for (String key : msgIds.keySet())
			sortedUids.add(Long.parseLong(key));
		return sortedUids.last();

	}

}
