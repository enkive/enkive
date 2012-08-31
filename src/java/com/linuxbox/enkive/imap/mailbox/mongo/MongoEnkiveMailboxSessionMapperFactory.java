package com.linuxbox.enkive.imap.mailbox.mongo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
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
		BasicDBObject subMailboxObject = new BasicDBObject();
		HashMap<String, String> msgIds = new HashMap<String, String>();
		msgIds.put(((Long.toString((long)1))), "e826e2fb14162842d9caf023163338d20e2820bb");
		msgIds.put(((Long.toString((long)2))), "0cc1d72e67c313e14202e96c3c6d78572af5398b");
		msgIds.put(((Long.toString((long)3))), "968e68f51976d94e7990c6b0d0301f36a62d5ea9");
		msgIds.put(((Long.toString((long)4))), "0f518127199b8b970e26ac980a531aa3c70a3296");
		msgIds.put(((Long.toString((long)5))), "e13d4968b68fafd59e44165428d6cbbd7c8b7f6c");
		msgIds.put(((Long.toString((long)6))), "3d81760c70b298abfc7f2843b42a68781779544f");
		msgIds.put(((Long.toString((long)7))), "b65f2b907b4dc2273e5fd4c0f5e0796e191e4cf2");
		
		mailboxObject.put("msgids", msgIds);
		subMailboxObject.put("msgids", msgIds);
		imapCollection.insert(mailboxObject);
		imapCollection.insert(subMailboxObject);
		ObjectId id = (ObjectId) mailboxObject.get("_id");
		ObjectId subId = (ObjectId) subMailboxObject.get("_id");
		
		BasicDBObject userMailboxesObject = new BasicDBObject();
		userMailboxesObject.put("user", "enkive");
		HashMap<String, String> mailboxTable = new HashMap<String, String>();
		mailboxTable.put("TESTFOLDER", id.toString());
		mailboxTable.put("TESTFOLDER/SUBFOLDER", subId.toString());
		mailboxTable.put("TESTFOLDER/SUBFOLDER/SUBSUBFOLDER", subId.toString());
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
