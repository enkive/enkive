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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.MailboxNotFoundException;
import org.apache.james.mailbox.model.MailboxConstants;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.store.mail.model.Mailbox;

import com.linuxbox.enkive.imap.mailbox.EnkiveImapMailbox;
import com.linuxbox.enkive.imap.mailbox.EnkiveImapMailboxMapper;
import com.linuxbox.enkive.imap.mongo.MongoEnkiveImapConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoEnkiveImapMailboxMapper extends EnkiveImapMailboxMapper {

	Mongo m;
	DB enkiveDB;
	DBCollection imapCollection;

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.imap");

	public MongoEnkiveImapMailboxMapper(MailboxSession session, Mongo m,
			String enkiveDbName, String imapCollectionName) {
		super(session);
		this.m = m;
		enkiveDB = m.getDB(enkiveDbName);
		imapCollection = enkiveDB.getCollection(imapCollectionName);
	}

	@Override
	public List<Mailbox<String>> list() throws MailboxException {
		ArrayList<Mailbox<String>> mailboxes = new ArrayList<Mailbox<String>>();
		DBObject mailboxListObject = getMailboxList();
		if (mailboxListObject != null) {
			HashMap<String, String> mailboxTable = (HashMap<String, String>) mailboxListObject
					.get(MongoEnkiveImapConstants.MAILBOXES);
			for (String mailboxKey : mailboxTable.keySet()) {
				MailboxPath mailboxPath = new MailboxPath(
						session.getPersonalSpace(), session.getUser()
								.getUserName(), mailboxKey.replace("/", "."));
				EnkiveImapMailbox mailbox = new EnkiveImapMailbox(mailboxPath,
						1);
				mailbox.setMailboxId(mailboxTable.get(mailboxKey));
				mailboxes.add(mailbox);
			}

		}
		MailboxPath inboxPath = new MailboxPath(session.getPersonalSpace(),
				session.getUser().getUserName(), MailboxConstants.INBOX);
		MailboxPath trashPath = new MailboxPath(session.getPersonalSpace(),
				session.getUser().getUserName(), "Trash");

		mailboxes.add(new EnkiveImapMailbox(inboxPath, 1));
		mailboxes.add(new EnkiveImapMailbox(trashPath, 1));
		return mailboxes;
	}

	@Override
	public Mailbox<String> findMailboxByPath(MailboxPath mailboxName)
			throws MailboxException, MailboxNotFoundException {
		DBObject mailboxListObject = getMailboxList();
		if (mailboxName.getName().equals(MailboxConstants.INBOX)) {
			MailboxPath inboxPath = new MailboxPath(session.getPersonalSpace(),
					session.getUser().getUserName(), MailboxConstants.INBOX);
			return new EnkiveImapMailbox(inboxPath, 1);
		} else if (mailboxName.getName().equals("Trash")) {
			MailboxPath trashPath = new MailboxPath(session.getPersonalSpace(),
					session.getUser().getUserName(), "Trash");
			return new EnkiveImapMailbox(trashPath, 1);

		} else if (mailboxListObject != null) {
			HashMap<String, String> mailboxTable = (HashMap<String, String>) mailboxListObject
					.get(MongoEnkiveImapConstants.MAILBOXES);
			String searchMailboxName = mailboxName.getName().replace(".", "/");
			if (mailboxTable.containsKey(searchMailboxName)) {
				mailboxName.setName(searchMailboxName);
				EnkiveImapMailbox mailbox = new EnkiveImapMailbox(mailboxName,
						1);
				mailbox.setMailboxId(mailboxTable.get(searchMailboxName));
				return mailbox;
			}
		}

		return null;
	}

	@Override
	public List<Mailbox<String>> findMailboxWithPathLike(MailboxPath mailboxPath) {
		String mailboxSearchPath = mailboxPath.getName();
		ArrayList<Mailbox<String>> mailboxes = new ArrayList<Mailbox<String>>();
		if (mailboxSearchPath.equals("%"))
			try {
				return list();
			} catch (MailboxException e) {
				LOGGER.error("Error retrieving list of mailboxes for user "
						+ session.getUser().getUserName(), e);
			}
		else if (mailboxPath.getName().matches(MailboxConstants.INBOX)) {
			MailboxPath inboxPath = new MailboxPath(session.getPersonalSpace(),
					session.getUser().getUserName(), MailboxConstants.INBOX);
			mailboxes.add(new EnkiveImapMailbox(inboxPath, 1));
		} else if (mailboxPath.getName().matches("Trash")) {
			MailboxPath trashPath = new MailboxPath(session.getPersonalSpace(),
					session.getUser().getUserName(), "Trash");
			mailboxes.add(new EnkiveImapMailbox(trashPath, 1));

		} else {
			DBObject mailboxListObject = getMailboxList();
			if (mailboxListObject != null) {
				HashMap<String, String> mailboxTable = (HashMap<String, String>) mailboxListObject
						.get(MongoEnkiveImapConstants.MAILBOXES);
				Set<String> mailboxPaths = mailboxTable.keySet();
				for (String mailboxKey : mailboxPaths) {
					String updatedMailboxKey = mailboxKey.replace('/', '.');
					String regex = mailboxSearchPath.replace(".", "+\\.+");
					regex = regex.replace('%', '.') + "*";
					if (updatedMailboxKey.matches(regex)) {
						MailboxPath matchingMailboxPath = new MailboxPath(
								session.getPersonalSpace(), session.getUser()
										.getUserName(), updatedMailboxKey);
						EnkiveImapMailbox mailbox = new EnkiveImapMailbox(
								matchingMailboxPath, 1);
						mailbox.setMailboxId(mailboxTable.get(mailboxKey));
						mailboxes.add(mailbox);
					}

				}
			}
		}
		return mailboxes;
	}

	@Override
	public boolean hasChildren(Mailbox<String> mailbox, char delimiter)
			throws MailboxException, MailboxNotFoundException {
		// TODO FIXME
		if (mailbox.getName().equals("INBOX")
				|| mailbox.getName().equals("Trash"))
			return false;
		return true;
	}

	protected DBObject getMailboxList() {
		String user = session.getUser().getUserName();
		DBObject mailboxQuery = new BasicDBObject();
		mailboxQuery.put(MongoEnkiveImapConstants.USER, user);
		return imapCollection.findOne(mailboxQuery);
	}

	protected DBObject buildMailboxQuery(String mailboxPath) {
		String user = session.getUser().getUserName();
		DBObject mailboxQuery = new BasicDBObject();
		mailboxQuery.put(MongoEnkiveImapConstants.USER, user);
		return mailboxQuery;
	}

}
