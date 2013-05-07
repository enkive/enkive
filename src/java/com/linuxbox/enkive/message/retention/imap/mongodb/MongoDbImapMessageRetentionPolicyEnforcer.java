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
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoDbImapMessageRetentionPolicyEnforcer extends
		MongodbMessageRetentionPolicyEnforcer {

	DBCollection imapColl;

	public MongoDbImapMessageRetentionPolicyEnforcer(Mongo m,
			String imapDbName, String imapCollectionName) {
		this(m.getDB(imapDbName).getCollection(imapCollectionName));
	}
	
	public MongoDbImapMessageRetentionPolicyEnforcer(MongoDbInfo dbInfo) {
		this(dbInfo.getCollection());
	}

	public MongoDbImapMessageRetentionPolicyEnforcer(DBCollection collection) {
		this.imapColl = collection;
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
				LOGGER.error("Error removing imap folder for user "
						+ userMailboxList.get(MongoEnkiveImapConstants.USER), e);
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
		BasicDBObject userMailboxesSearchObject = new BasicDBObject(
				MongoEnkiveImapConstants.USER, username);
		DBObject userMailboxesObject = imapColl
				.findOne(userMailboxesSearchObject);
		// Check for mailbox we're looking for
		@SuppressWarnings("unchecked")
		HashMap<String, String> mailboxTable = (HashMap<String, String>) userMailboxesObject
				.get(MongoEnkiveImapConstants.MAILBOXES);
		// If it exists, remove it
		if (mailboxTable.containsKey(mailboxPath)) {
			BasicDBObject mailboxSearchObject = new BasicDBObject("_id",
					ObjectId.massageToObjectId(mailboxTable.get(mailboxPath)));
			imapColl.remove(mailboxSearchObject);
			mailboxTable.remove(mailboxPath);
			userMailboxesObject.put(MongoEnkiveImapConstants.MAILBOXES,
					mailboxTable);
			imapColl.findAndModify(userMailboxesSearchObject,
					userMailboxesObject);
		}
	}

}
