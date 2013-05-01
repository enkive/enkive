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

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.mail.MailboxMapper;
import org.apache.james.mailbox.store.mail.MessageMapper;

import com.linuxbox.enkive.imap.mailbox.EnkiveMailboxSessionMapperFactory;
import com.linuxbox.enkive.imap.message.mongo.MongoEnkiveImapMessageMapper;
import com.linuxbox.enkive.imap.mongo.MongoEnkiveImapStore;
import com.linuxbox.util.dbinfo.mongodb.MongoDBInfo;
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

	public MongoEnkiveMailboxSessionMapperFactory(MongoDBInfo dbInfo) {
		this(dbInfo.getMongo(), dbInfo.getDbName(), dbInfo.getCollectionName());
	}

	@Override
	protected MailboxMapper<String> createMailboxMapper(MailboxSession session)
			throws MailboxException {
		return new MongoEnkiveImapMailboxMapper(session, m, enkiveDBName,
				imapCollectionName);
	}

	/**
	 * FIXME: this code smells -- we need to see if this can be simplified
	 */
	@Override
	protected MessageMapper<String> createMessageMapper(MailboxSession session)
			throws MailboxException {
		return new MongoEnkiveImapMessageMapper(session,
				new MongoEnkiveImapStore(m, enkiveDBName, imapCollectionName),
				retrieverService, m, enkiveDBName, imapCollectionName);
	}

}
