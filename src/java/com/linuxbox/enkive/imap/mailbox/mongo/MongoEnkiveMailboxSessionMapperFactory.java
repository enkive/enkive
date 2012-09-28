package com.linuxbox.enkive.imap.mailbox.mongo;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.mail.MailboxMapper;
import org.apache.james.mailbox.store.mail.MessageMapper;

import com.linuxbox.enkive.imap.mailbox.EnkiveMailboxSessionMapperFactory;
import com.linuxbox.enkive.imap.message.mongo.MongoEnkiveImapMessageMapper;
import com.linuxbox.enkive.imap.mongo.MongoEnkiveImapStore;
import com.mongodb.Mongo;

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
		return new MongoEnkiveImapMessageMapper(session,
				new MongoEnkiveImapStore(m, enkiveDBName, imapCollectionName),
				retrieverService, m, enkiveDBName, imapCollectionName);
	}

}
