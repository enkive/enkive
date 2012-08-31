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
		ArrayList<Mailbox<String>> mailboxes = new ArrayList<Mailbox<String>>();
		DBObject mailboxListObject = getMailboxList();
		if (mailboxListObject != null) {
			HashMap<String, String> mailboxTable = (HashMap<String, String>) mailboxListObject
					.get("mailboxes");
			for (String mailboxKey : mailboxTable.keySet()) {
				System.out.println("listing mailbox " + mailboxKey);
				MailboxPath mailboxPath = new MailboxPath(
						session.getPersonalSpace(), session.getUser()
								.getUserName(), mailboxKey.replace("/", "."));
				EnkiveImapMailbox mailbox = new EnkiveImapMailbox(mailboxPath,
						System.currentTimeMillis());
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
			String searchMailboxName = mailboxName.getName().replace(".", "/");
			System.out.println("looking for mailbox " + searchMailboxName);
			if (mailboxTable.containsKey(searchMailboxName)) {
				// TODO Need to get correct uidvalidity
				mailboxName.setName(searchMailboxName);
				EnkiveImapMailbox mailbox = new EnkiveImapMailbox(mailboxName,
						System.currentTimeMillis());
				mailbox.setMailboxId(mailboxTable.get(searchMailboxName));
				return mailbox;
			} else {
				System.out.println("Trying to find mailbox "
						+ mailboxName.getName());
			}
		}

		return null;
	}

	@Override
	public List<Mailbox<String>> findMailboxWithPathLike(MailboxPath mailboxPath) {
		String mailboxSearchPath = mailboxPath.getName();
		System.out.println("FINDING MAILBOX LIKE " + mailboxSearchPath);
		ArrayList<Mailbox<String>> mailboxes = new ArrayList<Mailbox<String>>();
		if (mailboxSearchPath.equals("%"))
			try {
				return list();
			} catch (MailboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
						.get("mailboxes");
				Set<String> mailboxPaths = mailboxTable.keySet();
				for (String mailboxKey : mailboxPaths) {
					String updatedMailboxKey = mailboxKey.replace('/', '.');
					System.out.println("SEARCHING FOR MAILBOX " + mailboxSearchPath);
					String regex = mailboxSearchPath.replace(".", "+\\.+");
					regex = regex.replace('%', '.') + "*";
					if (updatedMailboxKey.matches(regex)) {
						System.out.println("REGEX MATCHED " + updatedMailboxKey + " " + regex);
						MailboxPath matchingMailboxPath = new MailboxPath(
								session.getPersonalSpace(), session.getUser()
										.getUserName(), updatedMailboxKey);
						EnkiveImapMailbox mailbox = new EnkiveImapMailbox(
								matchingMailboxPath, System.currentTimeMillis());
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
		// TODO Auto-generated method stub
		System.out.println("HAS CHILDREN " + mailbox.getName() + " DELIMITER "
				+ delimiter);
		return true;
	}

	protected DBObject getMailboxList() {
		String user = session.getUser().getUserName();
		DBObject mailboxQuery = new BasicDBObject();
		mailboxQuery.put("user", user);
		return imapCollection.findOne(mailboxQuery);
	}

	protected DBObject buildMailboxQuery(String mailboxPath) {
		String user = session.getUser().getUserName();
		DBObject mailboxQuery = new BasicDBObject();
		mailboxQuery.put("user", user);
		return mailboxQuery;
	}

}
