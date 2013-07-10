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
package com.linuxbox.enkive.message.search.mongodb;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CC;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MAIL_FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.RCPT_TO;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.TO;
import static com.linuxbox.enkive.search.Constants.LIMIT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.RECIPIENT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.SENDER_PARAMETER;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.message.search.AbstractMessageSearchService;
import com.linuxbox.enkive.message.search.exception.EmptySearchResultsException;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoMessageSearchService extends AbstractMessageSearchService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.searchService.mongodb");

	// variables for query builders

	protected final static String[] RECIPIENT_QUERIES = { RCPT_TO, TO, CC };
	protected final static String[] SENDER_QUERIES = { MAIL_FROM, FROM };

	protected RecipientQueryBuilder recipientQueryBuilder;
	protected RecipientQueryBuilder senderQueryBuilder;
	protected MongoMessageQueryBuilder[] queryBuilders;

	// MongoDB variables

	protected final static DBObject ID_ONLY_QUERY = BasicDBObjectBuilder
			.start().add("_id", 1).get();

	protected DBCollection messageColl;

	public MongoMessageSearchService(MongoClient m, String dbName, String collName) {
		this(m.getDB(dbName), collName);
	}

	public MongoMessageSearchService(DB db, String collName) {
		this(db.getCollection(collName));
	}

	public MongoMessageSearchService(MongoDbInfo dbInfo) {
		this(dbInfo.getCollection());
	}

	public MongoMessageSearchService(DBCollection collection) {
		super();
		this.messageColl = collection;
	}

	/**
	 * Finish initialization once service has been set up, specifically once we
	 * know that docSearchService has been set.
	 */
	@PostConstruct
	public void finishSetup() {
		recipientQueryBuilder = new RecipientQueryBuilder(RECIPIENT_PARAMETER,
				RECIPIENT_QUERIES);
		senderQueryBuilder = new RecipientQueryBuilder(SENDER_PARAMETER,
				SENDER_QUERIES);
		MongoMessageQueryBuilder[] queryBuilders = { new DateQueryBuilder(),
				new SubjectQueryBuilder(), new MessageIdQueryBuilder(),
				new ContentQueryBuilder(docSearchService),
				recipientQueryBuilder, senderQueryBuilder,
				new PermissionsQueryBuilder(), new  InitialMessageUUIDQueryBuilder()};
		this.queryBuilders = queryBuilders;
	}

	public Set<String> searchImpl(Map<String, String> fields)
			throws MessageSearchException {
		Set<String> messageIds = new HashSet<String>();
		try {
			DBObject query = buildQueryObject(fields);

			DBCursor results = messageColl.find(query, ID_ONLY_QUERY);
			String limitStr = fields.get(LIMIT_PARAMETER);
			if (limitStr != null) {
				limitStr = limitStr.trim();
				if (!limitStr.isEmpty()) {
					try {
						int limit = Integer.parseInt(fields
								.get(LIMIT_PARAMETER));
						results.limit(limit);
					} catch (NumberFormatException e) {
						LOGGER.warn("Could not parse limit argument \""
								+ limitStr + "\" into integer.", e);
					}
				}
			}

			while (results.hasNext()) {
				DBObject message = results.next();
				messageIds.add((String) message.get("_id"));
			}
		} catch (EmptySearchResultsException e) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("A query that produced know results was executed.",
						e);
			}
		}

		return messageIds;
	}

	/*
	 * TODO: It's sad that some of the of the callers (indirect) to this method
	 * already have parsed data; yet they convert the data to strings, push them
	 * into a map, and then we re-parse the data!? Some day I hope we can fix
	 * this.
	 */
	protected DBObject buildQueryObject(Map<String, String> fields)
			throws MessageSearchException, EmptySearchResultsException {
		BasicDBList conjunctionList = new BasicDBList();

		for (MongoMessageQueryBuilder queryBuilder : queryBuilders) {
			DBObject query = queryBuilder.buildQueryPortion(fields);
			if (null != query) {
				conjunctionList.add(query);
			}
		}

		DBObject result;

		if (conjunctionList.isEmpty()) {
			throw new EmptySearchResultsException(
					"No understandable search terms specified.");
		} else if (conjunctionList.size() == 1) {
			result = (DBObject) conjunctionList.get(0);
		} else {
			result = new BasicDBObject();
			result.put("$and", conjunctionList);
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("MongoDB message search query is: " + result);
		}

		return result;
	}

	@Override
	public boolean cancelAsyncSearch(String searchId)
			throws MessageSearchException {
		throw new MessageSearchException("Unimplemented");
	}
}
