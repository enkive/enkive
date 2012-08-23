package com.linuxbox.enkive.imap.mailbox.mongo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.mail.MailboxMapper;
import org.apache.james.mailbox.store.mail.MessageMapper;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.imap.EnkiveImapStore;
import com.linuxbox.enkive.imap.mailbox.EnkiveMailboxSessionMapperFactory;
import com.linuxbox.enkive.imap.message.mongo.MongoEnkiveImapMessageMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;

public class MongoEnkiveMailboxSessionMapperFactory extends
		EnkiveMailboxSessionMapperFactory {

	Mongo m;
	String enkiveDBName;
	String imapCollectionName;

	public MongoEnkiveMailboxSessionMapperFactory(Mongo m, String enkiveDBName,
			String imapCollectionName) {
		this.m = m;
		this.enkiveDBName = enkiveDBName;
		this.imapCollectionName = imapCollectionName;
		
		DB enkiveDB = m.getDB(enkiveDBName);
		DBCollection imapCollection = enkiveDB.getCollection(imapCollectionName);
		
		BasicDBObject mailboxObject = new BasicDBObject();
		Set<String> msgIds = new HashSet<String>();
		msgIds.add("e826e2fb14162842d9caf023163338d20e2820bb");
		mailboxObject.put("msgids", msgIds);
		imapCollection.insert(mailboxObject);
		ObjectId id = (ObjectId) mailboxObject.get("_id");
		
		BasicDBObject userMailboxesObject = new BasicDBObject();
		userMailboxesObject.put("user", "enkive");
		HashMap<String, String> mailboxTable = new HashMap<String, String>();
		mailboxTable.put("TESTFOLDER", id.toString());
		userMailboxesObject.put("mailboxes", mailboxTable);
		imapCollection.insert(userMailboxesObject);
		
	}

	@Override
	protected MailboxMapper<String> createMailboxMapper(MailboxSession session)
			throws MailboxException {
		return new MongoEnkiveImapMailboxMapper(session, m, enkiveDBName,
				imapCollectionName);
	}

	@Override
	protected MessageMapper<String> createMessageMapper(MailboxSession session)
			throws MailboxException {
		return new MongoEnkiveImapMessageMapper(session, new EnkiveImapStore(),
				retrieverService, m, enkiveDBName, imapCollectionName);
	}

}
