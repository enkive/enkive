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
package com.linuxbox.enkive.audit.mongodb;

import static com.linuxbox.util.mongodb.MongoDBConstants.CALL_ENSURE_INDEX_ON_INIT;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.BSONTimestamp;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.audit.AuditEntry;
import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.linuxbox.util.mongodb.MongoDBConstants;
import com.linuxbox.util.mongodb.MongoIndexable;
import com.linuxbox.util.queueservice.QueueServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class MongoAuditService implements AuditService, MongoIndexable {
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.audit.mongodb");

	/**
	 * TODO: We have to decide whether the system will wait until the audit log
	 * entries are in storage or whether we can continue before so. If this is
	 * set to true, the audit log will likely be slower but safer.
	 */
	private static final boolean CONFIRM_AUDIT_LOG_WRITES = false;

	private static final String TIMESTAMP_FIELD = "tstamp";
	private static final String CODE_FIELD = "code";
	private static final String USERNAME_FIELD = "user";
	private static final String DESCRIPTION_FIELD = "desc";

	private static final String TIMESTAMP_INDEX = "timestampIndex";
	private static final String CODE_TIMESTAMP_INDEX = "codeTimestampIndex";
	private static final String USER_TIMESTAMP_INDEX = "userTimestampIndex";

	@SuppressWarnings("unused")
	private static DBObject DATE_FORWARD_SORT = new BasicDBObject(
			TIMESTAMP_FIELD, 1);
	private static DBObject DATE_BACKWARD_SORT = new BasicDBObject(
			TIMESTAMP_FIELD, -1);

	private final Mongo mongo;
	private final DBCollection auditCollection;

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
		this(mongo, mongo.getDB(database).getCollection(collection));
	}

	public MongoAuditService(MongoDbInfo dbInfo) {
		this(dbInfo.getMongo(), dbInfo.getCollection());
	}

	public MongoAuditService(Mongo mongo, DBCollection collection) {
		this.mongo = mongo;
		this.auditCollection = collection;

		// see comments on def'n of CALL_ENSURE_INDEX_ON_INIT to see why it's
		// done conditionally
		if (CALL_ENSURE_INDEX_ON_INIT) {
			// see class com.linuxbox.enkive.MongoDBIndexManager
		}

		// TODO: do we (will we) need a who, what, when index, so we can select
		// by who/what and sort by when?

		if (CONFIRM_AUDIT_LOG_WRITES) {
			auditCollection.setWriteConcern(WriteConcern.FSYNC_SAFE);
		} else {
			auditCollection.setWriteConcern(WriteConcern.NORMAL);
		}

		final int indexCount = auditCollection.getIndexInfo().size();
		// we expect 4 -- our 3 plus the default index on ObjectID
		if (indexCount > 4) {
			if (LOGGER.isWarnEnabled())
				LOGGER.warn("the MongoAuditService may have extra indices (which could impact performance); expect 4 but have "
						+ indexCount);
		}
	}

	public void startup() throws QueueServiceException {
		// empty
	}

	public void shutdown() throws QueueServiceException {
		if (createdMongo) {
			mongo.close();
		}
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("shut down MongoAuditLogService");
	}

	@Override
	public void addEvent(int eventCode, String userIdentifier,
			String description) throws AuditServiceException {
		final DBObject insert = new BasicDBObject()
				.append(TIMESTAMP_FIELD, new BSONTimestamp())
				.append(CODE_FIELD, eventCode)
				.append(USERNAME_FIELD, userIdentifier)
				.append(DESCRIPTION_FIELD, description);
		final WriteResult result = auditCollection.insert(insert);

		if (CONFIRM_AUDIT_LOG_WRITES) {
			if (!result.getLastError().ok()) {
				throw new AuditServiceException(
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
			throws AuditServiceException {
		addEvent(eventCode, userIdentifier, description);
	}

	@Override
	public AuditEntry getEvent(String identifier) throws AuditServiceException {
		final ObjectId idObject = ObjectId.massageToObjectId(identifier);
		final QueryBuilder queryBuilder = QueryBuilder.start(
				MongoDBConstants.OBJECT_ID_KEY).is(idObject);
		final DBObject resultObject = auditCollection.findOne(queryBuilder
				.get());
		return dbObjectToAuditEntry(resultObject);
	}

	/**
	 * If there is a username, then we hint to use that index, as we expect
	 * there to be many users compared to event codes. Thus it is likely that
	 * generally filtering by users will reduce the result set by the greatest
	 * amount. Of course some event codes are more prevalent than others, so
	 * this won't always be the case.
	 * 
	 * @param startTime
	 *            all results must occur ON or AFTER this timestamp
	 * @param endTime
	 *            all results must occur BEFORE this timestamp
	 * @throws AuditServiceException
	 */
	@Override
	public List<AuditEntry> search(Integer eventCode, String userIdentifier,
			Date startTime, Date endTime) throws AuditServiceException {
		final QueryBuilder qb = QueryBuilder.start();
		String indexHint = TIMESTAMP_INDEX;

		if (eventCode != null) {
			qb.put(CODE_FIELD).is(eventCode);
			indexHint = CODE_TIMESTAMP_INDEX;
		}
		if (userIdentifier != null) {
			qb.put(USERNAME_FIELD).is(userIdentifier);
			indexHint = CODE_TIMESTAMP_INDEX;
		}
		if (startTime != null) {
			final int epochTime = (int) (startTime.getTime() / 1000);
			final BSONTimestamp timestamp = new BSONTimestamp(epochTime, 0);
			qb.put(TIMESTAMP_FIELD).greaterThanEquals(timestamp);
		}
		if (endTime != null) {
			final int epochTime = (int) (Math.ceil(endTime.getTime() / 1000.0));
			final BSONTimestamp timestamp = new BSONTimestamp(epochTime, 0);
			qb.put(TIMESTAMP_FIELD).lessThan(timestamp);
		}

		final DBCursor cursor = auditCollection.find(qb.get())
				.sort(DATE_BACKWARD_SORT).hint(indexHint);
		return dbCursortoAuditEntryList(cursor);
	}

	@Override
	public List<AuditEntry> getMostRecentByPage(int perPage, int pagesToSkip)
			throws AuditServiceException {
		final DBCursor cursor = auditCollection.find().sort(DATE_BACKWARD_SORT)
				.skip(perPage * pagesToSkip).limit(perPage);
		return dbCursortoAuditEntryList(cursor);
	}

	@Override
	public long getAuditEntryCount() throws AuditServiceException {
		return auditCollection.count();
	}

	/**
	 * Takes a DBCursor to a result containing AuditService entries and returns
	 * a List of those AuditEntry(s).
	 * 
	 * @param cursor
	 * @return
	 */
	private List<AuditEntry> dbCursortoAuditEntryList(DBCursor cursor) {
		List<AuditEntry> list = new ArrayList<AuditEntry>();
		while (cursor.hasNext()) {
			final DBObject entry = cursor.next();
			list.add(dbObjectToAuditEntry(entry));
		}
		return list;
	}

	/**
	 * Takes a DBObject to a result containing an AuditService entry and returns
	 * the corresponding AuditEntry.
	 * 
	 * @param cursor
	 * @return
	 */
	private AuditEntry dbObjectToAuditEntry(DBObject entry) {
		final String objectId = entry.get(MongoDBConstants.OBJECT_ID_KEY)
				.toString();
		final BSONTimestamp entryTimestamp = (BSONTimestamp) entry
				.get(TIMESTAMP_FIELD);

		// if we don't use long math, we get a roll-over; the cast is
		// unnecessary other than to emphasize this fact
		final Date entryDate = new Date(1000L * (long) entryTimestamp.getTime());

		final int entryCode = (Integer) entry.get(CODE_FIELD);
		final String user = (String) entry.get(USERNAME_FIELD);
		final String description = (String) entry.get(DESCRIPTION_FIELD);

		final AuditEntry auditEntry = new AuditEntry(objectId, entryDate,
				entryCode, user, description);
		return auditEntry;
	}

	@Override
	public List<DBObject> getIndexInfo() {
		return auditCollection.getIndexInfo();
	}

	@Override
	public List<IndexDescription> getPreferredIndexes() {
		List<IndexDescription> result = new LinkedList<IndexDescription>();

		// For MongoDB multi-key indexes to work the most efficiently, the
		// queried fields should appear before the sorting fields in the
		// indexes.

		final DBObject whenIndex = new BasicDBObject(TIMESTAMP_FIELD, -1);
		IndexDescription id1 = new IndexDescription(TIMESTAMP_INDEX, whenIndex,
				false);
		result.add(id1);

		final DBObject whatWhenIndex = new BasicDBObject(CODE_FIELD, 1).append(
				TIMESTAMP_FIELD, -1);
		IndexDescription id2 = new IndexDescription(CODE_TIMESTAMP_INDEX,
				whatWhenIndex, false);
		result.add(id2);

		final DBObject whoWhenIndex = new BasicDBObject(USERNAME_FIELD, 1)
				.append(TIMESTAMP_FIELD, -1);
		IndexDescription id3 = new IndexDescription(USER_TIMESTAMP_INDEX,
				whoWhenIndex, false);
		result.add(id3);

		return result;
	}

	@Override
	public void ensureIndex(DBObject index, DBObject options)
			throws MongoException {
		auditCollection.ensureIndex(index, options);
	}

	@Override
	public long getDocumentCount() throws MongoException {
		return auditCollection.count();
	}
}
