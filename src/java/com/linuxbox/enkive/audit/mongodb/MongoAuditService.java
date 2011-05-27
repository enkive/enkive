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

		// Make sure data is written out to disk before operation is complete.
		queueCollection.setWriteConcern(WriteConcern.NORMAL);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void addEvent(int eventCode, String userIdentifier,
			String description, boolean truncateDescription)
			throws AuditTrailException {
		addEvent(eventCode, userIdentifier, description);
	}

	@Override
	public AuditEntry getEvent(String identifer) throws AuditTrailException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AuditEntry> search(Integer eventCode, String userIdentifier,
			Date startDate, Date endDate) throws AuditTrailException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AuditEntry> getMostRecentByPage(int perPage, int pagesToSkip)
			throws AuditTrailException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getAuditEntryCount() throws AuditTrailException {
		return auditCollection.count();
	}
}
