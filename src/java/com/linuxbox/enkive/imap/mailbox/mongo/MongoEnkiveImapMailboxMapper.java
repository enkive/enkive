package com.linuxbox.enkive.imap.mailbox.mongo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.MailboxNotFoundException;
import org.apache.james.mailbox.model.MailboxConstants;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.store.mail.model.Mailbox;

import com.linuxbox.enkive.imap.mailbox.EnkiveImapMailbox;
import com.linuxbox.enkive.imap.mailbox.EnkiveImapMailboxMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoEnkiveImapMailboxMapper extends EnkiveImapMailboxMapper {

	Mongo m;
	DB enkiveDB;
	DBCollection imapCollection;

	public MongoEnkiveImapMailboxMapper(MailboxSession session, Mongo m,
			String enkiveDbName, String imapCollectionName) {
		super(session);
		this.m = m;
		enkiveDB = m.getDB(enkiveDbName);
		imapCollection = enkiveDB.getCollection(imapCollectionName);
	}

	@Override
	public List<Mailbox<String>> list() throws MailboxException {
		System.out.println("listing");
		ArrayList<Mailbox<String>> mailboxes = new ArrayList<Mailbox<String>>();
		DBObject mailboxListObject = getMailboxList();
		if (mailboxListObject != null) {
			HashMap<String, String> mailboxTable = (HashMap<String, String>) mailboxListObject
					.get("mailboxes");
			System.out.println("listing " + mailboxTable.size());
			for (String mailboxKey : mailboxTable.keySet()) {
				MailboxPath mailboxPath = new MailboxPath(
						session.getPersonalSpace(), session.getUser()
								.getUserName(), mailboxKey);
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
					.get("mailboxes");
			if (mailboxTable.containsKey(mailboxName.getName())) {
				EnkiveImapMailbox mailbox = new EnkiveImapMailbox(mailboxName,
						1);
				mailbox.setMailboxId(mailboxTable.get(mailboxName.getName()));
				return mailbox;
			}
		}

		return null;
	}

	@Override
	public List<Mailbox<String>> findMailboxWithPathLike(MailboxPath mailboxPath) {
		String mailboxSearchPath = mailboxPath.getName();
		ArrayList<Mailbox<String>> mailboxes = new ArrayList<Mailbox<String>>();
		DBObject mailboxListObject = getMailboxList();
		if (mailboxListObject != null) {
			System.out.println("finding " + mailboxSearchPath);
			HashMap<String, String> mailboxTable = (HashMap<String, String>) mailboxListObject
					.get("mailboxes");
			Set<String> mailboxPaths = mailboxTable.keySet();
			for (String mailboxKey : mailboxPaths) {
				if (mailboxSearchPath.equals("%")
						|| mailboxKey.matches(mailboxSearchPath)) {
					MailboxPath matchingMailboxPath = new MailboxPath(
							session.getPersonalSpace(), session.getUser()
									.getUserName(), mailboxKey);
					EnkiveImapMailbox mailbox = new EnkiveImapMailbox(
							matchingMailboxPath, 4);
					mailbox.setMailboxId(mailboxTable.get(mailboxKey));
					mailboxes.add(mailbox);
				}

			}
		}
		if (mailboxPath.getName().matches(MailboxConstants.INBOX)) {
			System.out.println("Matched INBOX");
			MailboxPath inboxPath = new MailboxPath(session.getPersonalSpace(),
					session.getUser().getUserName(), MailboxConstants.INBOX);
			mailboxes.add(new EnkiveImapMailbox(inboxPath, 1));
		}
		if (mailboxPath.getName().matches("Trash")) {
			System.out.println("Matched Trash");
			MailboxPath trashPath = new MailboxPath(session.getPersonalSpace(),
					session.getUser().getUserName(), "Trash");
			mailboxes.add(new EnkiveImapMailbox(trashPath, 1));

		}
		return mailboxes;
	}

	@Override
	public boolean hasChildren(Mailbox<String> mailbox, char delimiter)
			throws MailboxException, MailboxNotFoundException {
		// TODO Auto-generated method stub
		return false;
	}

	protected DBObject getMailboxList() {
		String user = session.getUser().getUserName();
		System.out.println(user);
		DBObject mailboxQuery = new BasicDBObject();
		mailboxQuery.put("user", user);
		System.out.println(mailboxQuery.toString());
		return imapCollection.findOne(mailboxQuery);
	}

	protected DBObject buildMailboxQuery(String mailboxPath) {
		String user = session.getUser().getUserName();
		DBObject mailboxQuery = new BasicDBObject();
		mailboxQuery.put("user", user);
		return mailboxQuery;
	}

}
