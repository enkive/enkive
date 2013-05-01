/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
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
 *******************************************************************************/
/*
 * 
 */
package com.linuxbox.enkive.permissions.message;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CC;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MAIL_FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.RCPT_TO;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.TO;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.ATTACHMENT_ID_LIST;

import java.util.Collection;

import com.linuxbox.util.dbinfo.mongodb.MongoDBInfo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

public class MongoMessagePermissionsService implements
		MessagePermissionsService {

	protected DBCollection messageCollection;

	public MongoMessagePermissionsService(Mongo mongo, String dbName,
			String collName) {
		this(mongo.getDB(dbName).getCollection(collName));
	}
	
	public MongoMessagePermissionsService(MongoDBInfo dbInfo) {
		this(dbInfo.getCollection());
	}
	
	public MongoMessagePermissionsService(DBCollection collection) {
		this.messageCollection = collection;
	}

	@Override
	public boolean canReadAttachment(Collection<String> addresses,
			String attachmentId) {
		BasicDBObject query = new BasicDBObject();

		// Needs to match MAIL_FROM OR FROM
		BasicDBList addressQuery = new BasicDBList();
		for (String address : addresses) {
			addressQuery.add(new BasicDBObject(MAIL_FROM, address));
			addressQuery.add(new BasicDBObject(FROM, address));
			addressQuery.add(new BasicDBObject(RCPT_TO, address));
			addressQuery.add(new BasicDBObject(TO, address));
			addressQuery.add(new BasicDBObject(CC, address));
		}
		query.put("$or", addressQuery);
		query.put(ATTACHMENT_ID_LIST, attachmentId);
		DBCursor results = messageCollection.find(query);
		if (results.size() > 0)
			return true;
		else
			return false;
	}

}
