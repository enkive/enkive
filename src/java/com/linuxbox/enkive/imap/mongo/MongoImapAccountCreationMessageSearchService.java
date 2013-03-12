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

import java.util.Date;

import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoImapAccountCreationMessageSearchService extends
		MongoMessageSearchService {

	public MongoImapAccountCreationMessageSearchService(Mongo m, String dbName,
			String collName) {
		super(m, dbName, collName);
	}

	public Date getEarliestMessageDate(String username) {
		BasicDBObject searchObject = new BasicDBObject();
		BasicDBObject fieldLimitObject = new BasicDBObject();
		fieldLimitObject.put("date", 1);
		DBCursor cursor = messageColl.find(searchObject, fieldLimitObject)
				.sort(fieldLimitObject).limit(1);
		DBObject earliestDateObject = cursor.next();
		Date earliestDate = (Date) earliestDateObject.get("date");
		return earliestDate;
	}

	public Date getLatestMessageDate(String username) {
		BasicDBObject searchObject = new BasicDBObject();
		BasicDBObject fieldLimitObject = new BasicDBObject();
		fieldLimitObject.put("date", 1);
		DBCursor cursor = messageColl.find(searchObject, fieldLimitObject)
				.sort(new BasicDBObject("date", -1)).limit(1);
		DBObject latestDateObject = cursor.next();
		Date latestDate = (Date) latestDateObject.get("date");
		return latestDate;
	}

}
