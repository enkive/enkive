package com.linuxbox.enkive.imap.mongo;

import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.NUMERIC_SEARCH_FORMAT;
import static com.linuxbox.enkive.search.Constants.RECIPIENT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.SENDER_PARAMETER;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.imap.EnkiveImapAccountCreator;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoImapAccountCreator implements EnkiveImapAccountCreator {

	Mongo m;
	DB imapDB;
	DBCollection imapCollection;
	MongoImapAccountCreationMessageSearchService searchService;

	public MongoImapAccountCreator(Mongo m, String imapDBName,
			String imapCollname) {
		imapDB = m.getDB(imapDBName);
		imapCollection = imapDB.getCollection(imapCollname);
	}

	public void createImapAccount(String username)
			throws MessageSearchException {
		HashMap<String, String> mailboxTable = new HashMap<String, String>();

		Date earliestMessageDate = searchService
				.getEarliestMessageDate(username);
		Date latestMessageDate = searchService.getLatestMessageDate(username);

		Calendar mailboxTime = Calendar.getInstance();
		mailboxTime.setTime(earliestMessageDate);
		Calendar endTime = Calendar.getInstance();
		endTime.setTime(latestMessageDate);
		mailboxTime = DateUtils.round(mailboxTime, Calendar.MONTH);
		endTime = DateUtils.round(endTime, Calendar.MONTH);
		Calendar today = Calendar.getInstance();

		// If it's not the first of the month, we need to get up to, but not
		// including the current day of the current month
		if (today.get(Calendar.DATE) != 1)
			endTime.add(Calendar.MONTH, 1);

		while (mailboxTime.before(endTime)) {
			BasicDBObject mailboxObject = new BasicDBObject();
			String mailboxPath = MongoEnkiveImapConstants.ARCHIVEDMESSAGESFOLDERNAME
					+ "/"
					+ mailboxTime.get(Calendar.YEAR)
					+ "/"
					+ String.format("%02d", mailboxTime.get(Calendar.MONTH) + 1);
			Calendar toDate = (Calendar) mailboxTime.clone();
			toDate.set(Calendar.DAY_OF_MONTH,
					mailboxTime.getActualMaximum(Calendar.DAY_OF_MONTH));

			// Get everything up until yesterday, nightly job will add today's
			// messages
			if (toDate.after(today)) {
				toDate = (Calendar) today.clone();
				toDate.add(Calendar.DATE, -1);
			}

			Set<String> msgIds = getMailboxMessageIds(username,
					mailboxTime.getTime(), toDate.getTime());
			if (!msgIds.isEmpty()) {
				if (!mailboxTable
						.containsKey(MongoEnkiveImapConstants.ARCHIVEDMESSAGESFOLDERNAME
								+ "/" + mailboxTime.get(Calendar.YEAR))) {
					BasicDBObject yearMailboxObject = new BasicDBObject();
					yearMailboxObject.put(MongoEnkiveImapConstants.MESSAGEIDS,
							new HashMap<String, String>());
					imapCollection.insert(yearMailboxObject);
					ObjectId id = (ObjectId) yearMailboxObject.get("_id");
					mailboxTable.put(
							MongoEnkiveImapConstants.ARCHIVEDMESSAGESFOLDERNAME
									+ "/" + mailboxTime.get(Calendar.YEAR),
							id.toString());
				}

				HashMap<String, String> mailboxMsgIds = new HashMap<String, String>();
				long i = 0;
				for (String msgId : msgIds) {
					i++;
					mailboxMsgIds.put(((Long.toString(i))), msgId);
				}
				mailboxObject.put(MongoEnkiveImapConstants.MESSAGEIDS,
						mailboxMsgIds);
				imapCollection.insert(mailboxObject);
				ObjectId id = (ObjectId) mailboxObject.get("_id");
				mailboxTable.put(mailboxPath, id.toString());
			}
			mailboxTime.add(Calendar.MONTH, 1);
		}
		// Setup Trash and Inbox
		BasicDBObject inboxObject = new BasicDBObject();
		String inboxPath = "INBOX";
		inboxObject.put(MongoEnkiveImapConstants.MESSAGEIDS,
				new HashMap<String, String>());
		imapCollection.insert(inboxObject);
		ObjectId inboxId = (ObjectId) inboxObject.get("_id");
		mailboxTable.put(inboxPath, inboxId.toString());

		BasicDBObject trashObject = new BasicDBObject();
		String trashPath = "Trash";
		trashObject.put(MongoEnkiveImapConstants.MESSAGEIDS,
				new HashMap<String, String>());
		imapCollection.insert(trashObject);
		ObjectId trashId = (ObjectId) inboxObject.get("_id");
		mailboxTable.put(trashPath, trashId.toString());

		BasicDBObject rootMailboxObject = new BasicDBObject();
		imapCollection.insert(rootMailboxObject);
		ObjectId id = (ObjectId) rootMailboxObject.get("_id");
		BasicDBObject userMailboxesObject = new BasicDBObject();
		userMailboxesObject.put(MongoEnkiveImapConstants.USER, username);
		mailboxTable.put(MongoEnkiveImapConstants.ARCHIVEDMESSAGESFOLDERNAME,
				id.toString());

		userMailboxesObject.put(MongoEnkiveImapConstants.MAILBOXES,
				mailboxTable);
		imapCollection.insert(userMailboxesObject);
	}

	private Set<String> getMailboxMessageIds(String username, Date fromDate,
			Date toDate) throws MessageSearchException {
		HashMap<String, String> fields = new HashMap<String, String>();

		fields.put(SENDER_PARAMETER, username);
		fields.put(RECIPIENT_PARAMETER, username);
		fields.put(DATE_EARLIEST_PARAMETER,
				NUMERIC_SEARCH_FORMAT.format(fromDate));
		fields.put(DATE_LATEST_PARAMETER, NUMERIC_SEARCH_FORMAT.format(toDate));
		return searchService.searchImpl(fields);
	}

	public MongoImapAccountCreationMessageSearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(
			MongoImapAccountCreationMessageSearchService searchService) {
		this.searchService = searchService;
	}

	@Override
	public boolean accountExists(String username) {
		BasicDBObject userMailboxesObject = new BasicDBObject();
		userMailboxesObject.put(MongoEnkiveImapConstants.USER, username);
		DBObject resultObject = imapCollection.findOne(userMailboxesObject);
		return (resultObject != null);

	}

	@Override
	public void addImapMessages(String username, Date fromDate, Date toDate)
			throws MessageSearchException {
		//Need to check if folder exists, if not create it and add to user mailbox list
		
		//Need to get all messages to add
		Set<String> messageIdsToAdd = getMailboxMessageIds(username, fromDate, toDate);
		//Need to add messages
		//Get top UID, add from there
		
		// TODO Auto-generated method stub

	}

}
