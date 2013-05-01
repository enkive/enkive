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
package com.linuxbox.enkive.imap.mongo;

import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.NUMERIC_SEARCH_FORMAT;
import static com.linuxbox.enkive.search.Constants.PERMISSIONS_RECIPIENT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.PERMISSIONS_SENDER_PARAMETER;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.time.DateUtils;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.imap.EnkiveImapAccountCreator;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.permissions.PermissionService;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoImapAccountCreator implements EnkiveImapAccountCreator {

	DBCollection imapCollection;
	
	MongoImapAccountCreationMessageSearchService searchService;
	PermissionService permissionsService;

	public MongoImapAccountCreator(Mongo m, String imapDBName,
			String imapCollName) {
		this(m.getDB(imapDBName), imapCollName);
	}
	
	public MongoImapAccountCreator(DB db, String imapCollname) {
		this(db.getCollection(imapCollname));
	}
	
	public MongoImapAccountCreator(MongoDbInfo dbInfo) {
		this(dbInfo.getCollection());
	}

	public MongoImapAccountCreator(DBCollection collection) {
		this.imapCollection = collection;
	}

	public void createImapAccount(String username)
			throws MessageSearchException {
		HashMap<String, String> mailboxTable = new HashMap<String, String>();

		Date earliestMessageDate = searchService
				.getEarliestMessageDate(username);
		Date latestMessageDate = searchService.getLatestMessageDate(username);

		Calendar latestMessageCalendar = Calendar.getInstance();
		latestMessageCalendar.setTime(latestMessageDate);
		DateUtils.round(latestMessageCalendar, Calendar.DATE);
		Calendar today = Calendar.getInstance();
		DateUtils.round(today, Calendar.DATE);
		if (latestMessageCalendar.equals(today)) {
			latestMessageCalendar.add(Calendar.DATE, -1);
			latestMessageDate = latestMessageCalendar.getTime();
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

		addImapMessages(username, earliestMessageDate, latestMessageDate);
	}

	private Set<String> getMailboxMessageIds(String username, Date fromDate,
			Date toDate) throws MessageSearchException {
		HashMap<String, String> fields = new HashMap<String, String>();

		Collection<String> addresses = permissionsService
				.canReadAddresses(username);
		StringBuilder addressesString = new StringBuilder();
		for (String address : addresses) {
			addressesString.append(address);
			addressesString.append("; ");
		}

		fields.put(PERMISSIONS_SENDER_PARAMETER, addressesString.toString());
		fields.put(PERMISSIONS_RECIPIENT_PARAMETER, addressesString.toString());

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

		Calendar startTime = Calendar.getInstance();
		startTime.setTime(fromDate);
		Calendar endTime = Calendar.getInstance();
		endTime.setTime(toDate);

		while (startTime.before(endTime)) {
			Calendar endOfMonth = (Calendar) startTime.clone();
			endOfMonth.set(Calendar.DAY_OF_MONTH,
					endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

			if (endOfMonth.after(endTime))
				endOfMonth = (Calendar) endTime.clone();

			// Need to get all messages to add
			Set<String> messageIdsToAdd = getMailboxMessageIds(username,
					startTime.getTime(), endOfMonth.getTime());
			// Need to add messages
			// Get top UID, add from there
			if (!messageIdsToAdd.isEmpty()) {
				// Need to check if folder exists, if not create it and add to
				// user
				// mailbox list
				DBObject mailboxObject = getMessagesFolder(username,
						startTime.getTime());

				@SuppressWarnings("unchecked")
				HashMap<String, String> mailboxMsgIds = (HashMap<String, String>) mailboxObject
						.get(MongoEnkiveImapConstants.MESSAGEIDS);

				TreeMap<String, String> sortedMsgIds = new TreeMap<String, String>();
				sortedMsgIds.putAll(mailboxMsgIds);
				long i = 0;
				if (!sortedMsgIds.isEmpty())
					i = Long.valueOf(sortedMsgIds.lastKey());

				for (String msgId : messageIdsToAdd) {
					i++;
					mailboxMsgIds.put(((Long.toString(i))), msgId);
				}
				mailboxObject.put(MongoEnkiveImapConstants.MESSAGEIDS,
						mailboxMsgIds);

				imapCollection.findAndModify(new BasicDBObject("_id",
						mailboxObject.get("_id")), mailboxObject);
			}
			startTime.set(Calendar.DAY_OF_MONTH, 1);
			startTime.add(Calendar.MONTH, 1);
		}
	}

	private DBObject getMessagesFolder(String username, Date date) {
		Calendar mailboxTime = Calendar.getInstance();
		mailboxTime.setTime(date);
		DBObject mailboxObject = new BasicDBObject();
		String mailboxPath = MongoEnkiveImapConstants.ARCHIVEDMESSAGESFOLDERNAME
				+ "/"
				+ mailboxTime.get(Calendar.YEAR)
				+ "/"
				+ String.format("%02d", mailboxTime.get(Calendar.MONTH) + 1);

		// Get table of user mailboxes
		BasicDBObject userMailboxesSearchObject = new BasicDBObject();
		userMailboxesSearchObject.put(MongoEnkiveImapConstants.USER, username);
		DBObject userMailboxesObject = imapCollection
				.findOne(userMailboxesSearchObject);
		// Check for mailbox we're looking for
		@SuppressWarnings("unchecked")
		HashMap<String, String> mailboxTable = (HashMap<String, String>) userMailboxesObject
				.get(MongoEnkiveImapConstants.MAILBOXES);
		// If it exists, return the associated object
		// If it doesn't exist, create it, and any necessary upper level folders
		if (mailboxTable.containsKey(mailboxPath)) {
			DBObject mailboxSearchObject = new BasicDBObject();
			mailboxSearchObject.put("_id",
					ObjectId.massageToObjectId(mailboxTable.get(mailboxPath)));
			mailboxObject = imapCollection.findOne(mailboxSearchObject);
			return mailboxObject;
		} else {
			mailboxObject.put(MongoEnkiveImapConstants.MESSAGEIDS,
					new HashMap<String, String>());
			imapCollection.insert(mailboxObject);
			ObjectId id = (ObjectId) mailboxObject.get("_id");
			mailboxTable.put(mailboxPath, id.toString());
		}

		if (!mailboxTable
				.containsKey(MongoEnkiveImapConstants.ARCHIVEDMESSAGESFOLDERNAME
						+ "/" + mailboxTime.get(Calendar.YEAR))) {
			BasicDBObject yearMailboxObject = new BasicDBObject();
			yearMailboxObject.put(MongoEnkiveImapConstants.MESSAGEIDS,
					new HashMap<String, String>());
			imapCollection.insert(yearMailboxObject);
			ObjectId id = (ObjectId) yearMailboxObject.get("_id");
			mailboxTable.put(
					MongoEnkiveImapConstants.ARCHIVEDMESSAGESFOLDERNAME + "/"
							+ mailboxTime.get(Calendar.YEAR), id.toString());
		}
		userMailboxesObject.put(MongoEnkiveImapConstants.MAILBOXES,
				mailboxTable);
		imapCollection.findAndModify(userMailboxesSearchObject,
				userMailboxesObject);

		return mailboxObject;
	}

	public void updateImapAccounts() throws MessageSearchException {
		BasicDBObject userMailboxSearchObject = new BasicDBObject(
				MongoEnkiveImapConstants.USER, 1);
		DBCursor userMailboxes = imapCollection.find(new BasicDBObject(),
				userMailboxSearchObject);
		// For each user
		while (userMailboxes.hasNext()) {
			DBObject userMailboxList = userMailboxes.next();
			Calendar yesterday = Calendar.getInstance();
			yesterday.add(Calendar.DATE, -1);
			addImapMessages(
					(String) userMailboxList.get(MongoEnkiveImapConstants.USER),
					yesterday.getTime(), yesterday.getTime());
		}
	}

	public PermissionService getPermissionsService() {
		return permissionsService;
	}

	public void setPermissionsService(PermissionService permissionsService) {
		this.permissionsService = permissionsService;
	}
}
