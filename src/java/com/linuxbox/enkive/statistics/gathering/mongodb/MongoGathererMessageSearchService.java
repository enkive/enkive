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
package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.ARCHIVE_TIME;
import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_TYPE;
import static com.linuxbox.enkive.statistics.StatsConstants.SIMPLE_DATE;

import java.util.Date;
import java.util.HashMap;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;
import com.linuxbox.enkive.statistics.gathering.GathererMessageSearchService;
import com.linuxbox.util.dbinfo.mongodb.MongoDBInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

public class MongoGathererMessageSearchService extends
		MongoMessageSearchService implements GathererMessageSearchService {

	public MongoGathererMessageSearchService(Mongo m, String dbName,
			String collName) {
		super(m, dbName, collName);
	}
	
	public MongoGathererMessageSearchService(MongoDBInfo dbInfo) {
		super(dbInfo);
	}

	@Override
	public int getNumberOfMessages(Date startDate, Date endDate)
			throws MessageSearchException {
		String dateEarliest = new StringBuilder(SIMPLE_DATE.format(startDate))
				.toString();
		String dateLatest = new StringBuilder(SIMPLE_DATE.format(endDate))
				.toString();

		HashMap<String, String> fields = new HashMap<String, String>();
		fields.put(DATE_EARLIEST_PARAMETER, dateEarliest);
		fields.put(DATE_LATEST_PARAMETER, dateLatest);
		fields.put(DATE_TYPE, ARCHIVE_TIME);

		BasicDBObject query = buildQueryObject(fields);
		DBCursor results = messageColl.find(query);

		int numOfMessages = results.count();
		results.close();
		return numOfMessages;
	}

}
