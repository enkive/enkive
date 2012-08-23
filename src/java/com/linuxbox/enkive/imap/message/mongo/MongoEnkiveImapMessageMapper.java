package com.linuxbox.enkive.imap.message.mongo;

import java.util.ArrayList;
import java.util.List;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.mail.model.Mailbox;

import com.linuxbox.enkive.imap.EnkiveImapStore;
import com.linuxbox.enkive.imap.message.EnkiveImapMessageMapper;
import com.linuxbox.enkive.retriever.MessageRetrieverService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
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

	@Override
	public long countMessagesInMailbox(Mailbox<String> mailbox)
			throws MailboxException {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public List<String> getMailboxMessageIds(Mailbox<String> mailbox,
			long fromUid, long toUid, int limit) {
		System.out.println(mailbox.getMailboxId());
		List<String> messageIds = new ArrayList<String>();
		messageIds.add("e826e2fb14162842d9caf023163338d20e2820bb");
		return messageIds;
	}

}
