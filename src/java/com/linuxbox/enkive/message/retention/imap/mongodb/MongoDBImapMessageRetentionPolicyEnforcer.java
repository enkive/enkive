package com.linuxbox.enkive.message.retention.imap.mongodb;

import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.NUMERIC_SEARCH_FORMAT;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.bson.types.ObjectId;

import com.linuxbox.enkive.imap.mongo.MongoEnkiveImapConstants;
import com.linuxbox.enkive.message.retention.mongodb.MongodbMessageRetentionPolicyEnforcer;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoDBImapMessageRetentionPolicyEnforcer extends
		MongodbMessageRetentionPolicyEnforcer {

	Mongo m;
	DB imapDB;
	DBCollection imapColl;

	public MongoDBImapMessageRetentionPolicyEnforcer(Mongo m,
			String imapDBname, String imapCollectionName) {
		this.m = m;
		imapDB = m.getDB(imapDBname);
		imapColl = imapDB.getCollection(imapCollectionName);
	}

	@Override
	public void enforceMessageRetentionPolicies() {
		// Need to delete imap folders older than policy date for every user
		// Get all user mailboxes
		BasicDBObject userMailboxSearchObject = new BasicDBObject(
				MongoEnkiveImapConstants.USER, 1);
		DBCursor userMailboxes = imapColl.find(new BasicDBObject(),
				userMailboxSearchObject);
		// For each user
		while (userMailboxes.hasNext()) {
			DBObject userMailboxList = userMailboxes.next();
			String retentionPolicyDateString = retentionPolicy
					.retentionPolicyCriteriaToSearchFields().get(
							DATE_LATEST_PARAMETER);
			try {
				Date retentionPolicyDate = NUMERIC_SEARCH_FORMAT
						.parse(retentionPolicyDateString);
				// Find dates older than policy date
				findAndRemoveMessagesFolder(
						(String) userMailboxList
								.get(MongoEnkiveImapConstants.USER),
						retentionPolicyDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		super.enforceMessageRetentionPolicies();
	}

	private void findAndRemoveMessagesFolder(String username, Date date) {
		Calendar mailboxTime = Calendar.getInstance();
		mailboxTime.setTime(date);
		// Subtract a month, because we want to remove the previous month
		mailboxTime.add(Calendar.MONTH, -1);
		String mailboxPath = MongoEnkiveImapConstants.ARCHIVEDMESSAGESFOLDERNAME
				+ "/"
				+ mailboxTime.get(Calendar.YEAR)
				+ "/"
				+ String.format("%02d", mailboxTime.get(Calendar.MONTH) + 1);

		// Get table of user mailboxes
		BasicDBObject userMailboxesSearchObject = new BasicDBObject();
		userMailboxesSearchObject.put(MongoEnkiveImapConstants.USER, username);
		DBObject userMailboxesObject = imapColl
				.findOne(userMailboxesSearchObject);
		// Check for mailbox we're looking for
		@SuppressWarnings("unchecked")
		HashMap<String, String> mailboxTable = (HashMap<String, String>) userMailboxesObject
				.get(MongoEnkiveImapConstants.MAILBOXES);
		// If it exists, remove it
		if (mailboxTable.containsKey(mailboxPath)) {
			mailboxTable.remove(mailboxPath);
			DBObject mailboxSearchObject = new BasicDBObject();
			mailboxSearchObject.put("_id",
					ObjectId.massageToObjectId(mailboxTable.get(mailboxPath)));
			imapColl.remove(mailboxSearchObject);
			userMailboxesObject.put(MongoEnkiveImapConstants.MAILBOXES,
					mailboxTable);
			imapColl.findAndModify(userMailboxesSearchObject,
					userMailboxesObject);
		}
	}

}
