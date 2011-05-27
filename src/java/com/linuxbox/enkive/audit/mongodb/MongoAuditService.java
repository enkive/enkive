/*
 *  Copyright 2010-2011 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.enkive.audit.mongodb;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.BSONTimestamp;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.audit.AuditEntry;
import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.AuditTrailException;
import com.linuxbox.util.mongodb.MongoDBConstants;
import com.linuxbox.util.queueservice.QueueServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class MongoAuditService implements AuditService {
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.audit.mongodb");

	private static final boolean CONFIRM_AUIDIT_LOG_WRITES = false;

	private static final String TIMESTAMP_FIELD = "tstamp";
	private static final String CODE_FIELD = "code";
	private static final String USERNAME_FIELD = "user";
	private static final String DESCRIPTION_FIELD = "desc";

	@SuppressWarnings("unused")
	private static DBObject DATE_FORWARD_SORT = new BasicDBObject(
			TIMESTAMP_FIELD, 1);
	private static DBObject DATE_BACKWARD_SORT = new BasicDBObject(
			TIMESTAMP_FIELD, -1);

	private Mongo mongo;
	private DB mongoDB;
	private DBCollection auditCollection;

	/**
	 * Keep track of whether we created the instance of Mongo, as if so, we'll
	 * have to close it.
	 */
	private boolean createdMongo;

	public MongoAuditService(String server, int port, String database,
			String collection) throws UnknownHostException, MongoException {
		this(new Mongo(server, port), database, collection);
		createdMongo = true;
	}

	public MongoAuditService(String database, String collection)
			throws UnknownHostException, MongoException {
		this(new Mongo(), database, collection);
		createdMongo = true;
	}

	public MongoAuditService(Mongo mongo, String database, String collection) {
		this.mongo = mongo;
		this.mongoDB = mongo.getDB(database);
		this.auditCollection = mongoDB.getCollection(collection);

		// For MongoDB multi-key indexes to work the most efficiently, the
		// queried fields should appear before the sorting fields in the
		// indexes.

		final DBObject whenIndex = new BasicDBObject(TIMESTAMP_FIELD, 1);
		auditCollection.ensureIndex(whenIndex, "timestampIndex");

		final DBObject whatWhenIndex = new BasicDBObject(CODE_FIELD, 1).append(
				TIMESTAMP_FIELD, 1);
		auditCollection.ensureIndex(whatWhenIndex, "codeTimestampIndex");

		final DBObject whoWhenIndex = new BasicDBObject(USERNAME_FIELD, 1)
				.append(TIMESTAMP_FIELD, 1);
		auditCollection.ensureIndex(whoWhenIndex, "userTimestampIndex");

		// TODO: do we (will we) need a who, what, when index, so we can select
		// by who/what and sort by when?

		// TODO: this is an important decision, which is whether we need to hold
		// up until the audit log entry is written to disk, or whether we
		// continue asynchronously.
		if (CONFIRM_AUIDIT_LOG_WRITES) {
			auditCollection.setWriteConcern(WriteConcern.FSYNC_SAFE);
		} else {
			auditCollection.setWriteConcern(WriteConcern.NORMAL);
		}
	}

	public void startup() throws QueueServiceException {
		// empty
	}

	public void shutdown() throws QueueServiceException {
		if (createdMongo) {
			mongo.close();
		}
		LOGGER.trace("shut down MongoAuditLogService");
	}

	@Override
	public void addEvent(int eventCode, String userIdentifier,
			String description) throws AuditTrailException {
		final DBObject insert = new BasicDBObject()
				.append(TIMESTAMP_FIELD, new BSONTimestamp())
				.append(CODE_FIELD, eventCode)
				.append(USERNAME_FIELD, userIdentifier)
				.append(DESCRIPTION_FIELD, description);
		final WriteResult result = auditCollection.insert(insert);

		if (CONFIRM_AUIDIT_LOG_WRITES) {
			if (!result.getLastError().ok()) {
				throw new AuditTrailException(
						"could not write entry to audit log", result
								.getLastError().getException());
			}
		}
	}

	/**
	 * Since MongoDB does not impose limits on fields, let's just ignore
	 * truncateDescription.
	 */
	@Override
	public void addEvent(int eventCode, String userIdentifier,
			String description, boolean truncateDescription)
			throws AuditTrailException {
		addEvent(eventCode, userIdentifier, description);
	}

	@Override
	public AuditEntry getEvent(String identifer) throws AuditTrailException {
		final ObjectId idObject = ObjectId.massageToObjectId(identifer);
		final QueryBuilder queryBuilder = QueryBuilder.start(
				MongoDBConstants.OBJECT_ID_KEY).is(idObject);
		final DBObject resultObject = auditCollection.findOne(queryBuilder
				.get());
		return dbObjectToAuditEntry(resultObject);
	}

	@Override
	public List<AuditEntry> search(Integer eventCode, String userIdentifier,
			Date startDate, Date endDate) throws AuditTrailException {
		final QueryBuilder qb = QueryBuilder.start();

		if (eventCode != null) {
			qb.put(CODE_FIELD).is(eventCode);
		}
		if (userIdentifier != null) {
			qb.put(USERNAME_FIELD).is(userIdentifier);
		}
		if (startDate != null) {
			qb.put(TIMESTAMP_FIELD).greaterThanEquals(startDate);
		}
		if (endDate != null) {
			qb.put(TIMESTAMP_FIELD).lessThan(endDate);
		}

		final DBCursor cursor = auditCollection.find(qb.get()).sort(
				DATE_BACKWARD_SORT);
		return dbCursortoAuditEntryList(cursor);
	}

	@Override
	public List<AuditEntry> getMostRecentByPage(int perPage, int pagesToSkip)
			throws AuditTrailException {
		final DBCursor cursor = auditCollection.find().sort(DATE_BACKWARD_SORT)
				.skip(perPage * pagesToSkip).limit(perPage);
		return dbCursortoAuditEntryList(cursor);
	}

	@Override
	public long getAuditEntryCount() throws AuditTrailException {
		return auditCollection.count();
	}
	
	private List<AuditEntry> dbCursortoAuditEntryList(DBCursor cursor) {
		List<AuditEntry> list = new ArrayList<AuditEntry>();
		while (cursor.hasNext()) {
			final DBObject entry = cursor.next();
			list.add(dbObjectToAuditEntry(entry));
		}
		return list;
	}

	private AuditEntry dbObjectToAuditEntry(DBObject entry) {
		final String objectId = entry.get(MongoDBConstants.OBJECT_ID_KEY)
				.toString();
		final BSONTimestamp entryTimestamp = (BSONTimestamp) entry
				.get(TIMESTAMP_FIELD);
		final Date entryDate = new Date(1000 * entryTimestamp.getTime());
		final int entryCode = (Integer) entry.get(CODE_FIELD);
		final String user = (String) entry.get(USERNAME_FIELD);
		final String description = (String) entry.get(DESCRIPTION_FIELD);

		final AuditEntry auditEntry = new AuditEntry(objectId, entryDate,
				entryCode, user, description);
		return auditEntry;
	}
}
